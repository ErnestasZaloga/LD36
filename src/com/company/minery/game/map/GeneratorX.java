package com.company.minery.game.map;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.company.minery.game.GameAssets;

public final class GeneratorX {
	
	private static class ResourceFormatException extends RuntimeException {
		public ResourceFormatException(final String message) {
			super(message + ", review .tmx file");
		}
	}
	
	private static final class MapData {
		
		public static final class TilesetData {
			public static final class Tile {
				public boolean minable;
				public boolean decoration;
				
			}
			
			public static enum Type{
				Tiles, GridDecorations, StaticDecorations;
			}
			
			public Tile[] tileset;
			public Type type;
			public int firstGid;
			
			public static Type resolvetType(final Element properties) {
				final ResourceFormatException up = new ResourceFormatException("tileset has no type");
				
				if(properties == null) {
					throw up;
				} 
				else {
					final Array<Element> children = properties.getChildrenByName("property");
					for(final Element prop : children) {
						final String attribute = prop.getAttribute("name", null);
						if(attribute.equalsIgnoreCase("tiles")) {
							return TilesetData.Type.Tiles;
						} 
						else if(attribute.equalsIgnoreCase("GridDecorations")) {
							return TilesetData.Type.GridDecorations;
						} 				
						else if(attribute.equalsIgnoreCase("StaticDecorations")) {
							return TilesetData.Type.StaticDecorations;
						}
					}
				}
				
				throw new ResourceFormatException("Tileset type not found in properties");
			}
		}
		
		public static final class LayerData {
			public int x, y; // in tiles
			public int width, height; // in tiles
			
			public boolean main = false;
			public byte[] tilesInMap;
			public final Array<StaticDecoration> staticDecorations = new Array<StaticDecoration>(StaticDecoration.class);
			public final Array<Tunnel> layerTunnels = new Array<Tunnel>(Tunnel.class);
			
			public LayerData(final Element layerData) {
				
				x = layerData.getIntAttribute("x", 0);
				y = layerData.getIntAttribute("y", 0);
				width = layerData.getIntAttribute("width", 0);
				height = layerData.getIntAttribute("height", 0);
				
				// check if layers is main layer
				final Element properties = layerData.getChildByName("properties");
				if(properties != null) {
					for(int i = 0, n = properties.getChildCount(); i < n; i++) {
						if(properties.getChild(i).getBooleanAttribute("Main", false)) {
							main = true;
						}
					}
				}
				
				// Parse all placed tiles
				final Element data = layerData.getChildByName("data");
				
				tilesInMap = extractTilesFromXML(data, width, height);
			}
			
			public void extractObjectGroupDataAndTunnels(final Element objectGroupData) {
				final Array<StaticDecoration> decorations = this.staticDecorations;
				final Array<Tunnel> tunnels = this.layerTunnels;
				
				final Array<Element> objects = objectGroupData.getChildrenByName("object");
				for(final Element object : objects) {
					
					final String type = object.getAttribute("type", "null");
					
					if(type.equals("null")) {
						
						decorations.add(
								new StaticDecoration(object.getFloatAttribute("x"),
													 object.getFloatAttribute("y"),
													 
													 0, // we don't know width by now
													 0, // we don't know height by now
													 
													 object.getIntAttribute("gid")));
						
					}
					else if(type.equalsIgnoreCase("tunnel")) {
						
						layerTunnels.add(
								new Tunnel(object.getAttribute("name"),
										   object.getFloatAttribute("x"),
										   object.getFloatAttribute("y"),
										   object.getFloatAttribute("width"),
										   object.getFloatAttribute("height")));
					}
				}
			}
			
			public static byte[] extractTilesFromXML(final Element data,
													 final int width,
													 final int height) {
				
				final byte[] tiles = new byte[width * height];
				
				for(int i = 0, n = data.getChildCount(); i < n; i += 1) {
					// Convert to our indexing(tiled uses top left, we use bottom left as the first element).
					final int x = i % width;
					final int y = height - (i / width) - 1;
					
					// -128 to increase capacity of byte to 255
					final byte gid = (byte) (data.getChild(i).getIntAttribute("gid") - 128);
					final int tileIndex = y * width + x;
					
					tiles[tileIndex] = gid;
				}
				
				return tiles;
			}
		}
		
		private final Element data;
		
		public int mapWidth;
		public int mapHeight;
		public int mapX;
		public int mapY;
		
		public int tileWidth;
		public int tileHeight;
		
		public final Array<TilesetData> tilesets = new Array<TilesetData>();
		public final Array<LayerData> layers = new Array<LayerData>();
		
		public final Array<Tunnel> tunnels = new Array<Tunnel>();
		
		public MapData(final Element data) {
			this.data = data;
			
			extractDimmensions();
			
			extractLayers();
			
			extractTilesets();
		}
		
		/**Extracts width and height of map and single tile*/
		public void extractDimmensions() {
			final Element data = this.data;
			
			mapWidth = data.getIntAttribute("width");
			mapHeight = data.getIntAttribute("height");
			tileWidth = data.getIntAttribute("tilewidth");
			tileHeight = data.getIntAttribute("tileheight");
		}
		
		/**Extracts Layers and puts them in {@code layers} Array, also collects tunnels
		 * from object groups */
		public void extractLayers() {
			
			// Whole XML document
			final Element data = this.data;
			
			// get layer count
			final int elementCount = data.getChildCount();
			
			// for every layer merge it with next object groups
			final Array<LayerData> layers = this.layers;
			final Array<Tunnel> tunnels = this.tunnels;
			int layersCreated = 0;
			
			LayerData layerData = null;
			boolean mainLayerFound = false;
			
			for(int i = 0; i < elementCount; i++) {
				final Element element = data.getChild(i);
				
				if(element.getName().equalsIgnoreCase("layer")) {
					
					// Add previous layer to array if created and creates a new one
					if(layersCreated != 0) {
						layers.add(layerData);
					}
					
					layerData = new LayerData(element);
					layersCreated++;
					
					mainLayerFound = layerData.main || mainLayerFound;
					
					// Check if layer is mainLayer and if no other main layers
					if(layerData.main && mainLayerFound) {
						throw new ResourceFormatException("more than one main layer");
					}
				}
				else if(element.getName().equalsIgnoreCase("objectgroup")) {
					if(layersCreated == 0) {
						throw new ResourceFormatException("Object group before first layer");
					}
					layerData.extractObjectGroupDataAndTunnels(element);
					
					// Collects all loaded tunnels because tunnels are hold in map, not in layer
					tunnels.addAll(layerData.layerTunnels);
				}
				else {
					continue;
				}
			}
		}

		public void setMapPosition(final int x, final int y) {
			
		}
	
		public void extractTilesets() {
			final Element data = this.data;
			final Array<TilesetData> tilesets = this.tilesets;
			
			final Array<Element> tilesetDatas = data.getChildrenByName("tileset");
			
			for(final Element tilesetDataXML : tilesetDatas) {
				tilesets.add(extractTileset(tilesetDataXML));
			}
		}
		
		public TilesetData extractTileset(final Element data) {
			final TilesetData tilesetData = new TilesetData();
			
			tilesetData.firstGid = data.getIntAttribute("firstgid");
			
			// Get tileset type
			final Element props = data.getChildByName("properties");
			
			tilesetData.type = TilesetData.resolvetType(props);
			
			
			
			return null;
		}
	}

	
	public static Map generateTestMap(final GameAssets assets) {
		final MapData mapData = new MapData(assets.testMapXml);
		
		return null;
	}
}
