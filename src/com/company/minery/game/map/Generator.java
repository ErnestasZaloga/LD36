package com.company.minery.game.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.company.minery.game.GameAssets;

public final class Generator {
	
	private static final class SubMap {
		
		private static final class Layer {
			
			public final byte[] tiles;
			public StaticDecoration[] decorations;
		
			public Layer(final byte[] tiles, 
						 final StaticDecoration[] decorations) {
				
				this.tiles = tiles;
				this.decorations = decorations;
			}
		}
		
		public final int width;
		public final int height;
		public final Tile[] tileset;

		public final SubMap.Layer[] layers;
		public final int mainLayerIndex;
		public final MapLocation[] tunnels;
		
		public final float tileWidth;
		public final float tileHeight;
		
		public SubMap(final int width, 
					  final int height, 
					  final Tile[] tileset, 
					  final SubMap.Layer[] layers,
					  final int mainLayerIndex,
					  final MapLocation[] tunnels,
					  final float tileWidth,
					  final float tileHeight) {
			
			this.width = width;
			this.height = height;
			this.tileset = tileset;
			this.layers = layers;
			this.mainLayerIndex = mainLayerIndex;
			this.tunnels = tunnels;
			this.tileWidth = tileWidth;
			this.tileHeight = tileHeight;
		}
		
	}
	
	private static final MapAssetLoader assetLoader = new MapAssetLoader();
	
	public static Map generateTestMap(final GameAssets assets) {
		final SubMap testPartition = createPartition(assets.testMapXml);
		final Layer[] layers = convertLayers(testPartition, testPartition.layers, 0, 0);
		
		// Layers have to be reversed...
		final Array<Layer> layersArray = new Array<Layer>();
		layersArray.addAll(layers);
		layersArray.reverse();
		
		final int mainIndex = layersArray.size - testPartition.mainLayerIndex - 1;
		
		final Map subMap = new Map(assetLoader,
								   testPartition.tileWidth,
								   testPartition.tileHeight,
								   layersArray.toArray(Layer.class),
								   layersArray.get(mainIndex),
								   testPartition.mainLayerIndex,
								   testPartition.tunnels);
		
		subMap.assetLoader.load(subMap, assets);
		
		return subMap;
	}
	
	private static Layer[] convertLayers(final SubMap partitionData,
										 final SubMap.Layer[] partitionLayers,
										 final int x,
										 final int y) {
		
		final int n = partitionLayers.length;
		final Layer[] layers = new Layer[n];
		
		for(int i = 0; i < n; i += 1) {
			layers[i] = convertLayer(partitionData, partitionLayers[i], x, y);
		}
		
		return layers;
	}
	
	private static Layer convertLayer(final SubMap partitionData,
									  final SubMap.Layer partitionLayer,
									  final int x,
									  final int y) {
		
		return new Layer(createTiles(partitionData, partitionLayer.tiles, x, y), partitionLayer.decorations);
	}
	
	private static Tiles createTiles(final SubMap partitionData, 
									 final byte[] tiles, 
									 final int x, 
									 final int y) {
		
		final int width = partitionData.width;
		final int height = partitionData.height;
		
		return new Tiles(tiles, partitionData.tileset, x, y, width, height);
	}
	
	private static SubMap createPartition(final Element xmlRoot) {
		final int partitionWidth = xmlRoot.getIntAttribute("width");
		final int partitionHeight = xmlRoot.getIntAttribute("height");
		final int tileWidth = xmlRoot.getIntAttribute("tilewidth");
		final int tileHeight = xmlRoot.getIntAttribute("tileheight");
		final float heightInPixels = tileHeight * partitionHeight;
		
		final Tile[] tileset;
		
		// Parse tile set
		{
			final IntArray tileElementGids = new IntArray();
			final Array<Element> tileElements = new Array<Element>();
			final Array<Element> tilesetElements = xmlRoot.getChildrenByName("tileset");
			
			// Collect all of the tiles and their gids
			{
				final int n = tilesetElements.size;
				
				for(int i = 0; i < n; i += 1) {
					final Element tilesetElement = tilesetElements.get(i);
					final int firstGid = tilesetElement.getIntAttribute("firstgid");
					final int nn = tilesetElement.getChildCount();
					
					// ii = 1 because 0 element is tiles set type (tiles, grid decorations or static decorations)
					
					//tilesetsTypes.add(tilesetElement);
					for(int ii = 0; ii < nn; ii += 1) {
						final int gid = firstGid + ii - 1;
						tileElementGids.add(gid);
						tileElements.add(tilesetElement.getChild(ii));
					}
				}
			}
			
			tileset = new Tile[tileElements.size];
			
			// Parse the tiles in gid order.
			{
				final int n = tileElements.size;
				
				for(int i = 0; i < n; i += 1) {
					final int gid = i;
					
					for(int ii = 0; ii < n; ii += 1) {
						if(tileElementGids.get(ii) == gid) {
							final Element tileElement = tileElements.get(ii);
							
							boolean decoration = false;
							
							final Element tilePropertiesElement = tileElement.getChildByName("properties");
							if(tilePropertiesElement != null) {
								final int nn = tilePropertiesElement.getChildCount();
								for(int iii = 0; iii < nn; iii += 1) {
									final Element tilePropertyElement = tilePropertiesElement.getChild(iii);
									
									if(tilePropertyElement.getAttribute("name").equalsIgnoreCase("decoration") &&
									   Boolean.valueOf(tilePropertyElement.getAttribute("value"))) {
												
									   decoration = true;
									}
								}
							}
							
							// Build region lookup name
							if(tileElement.getChildByName("image") == null) {
								continue;
							}
							
							final Element imageElement = tileElement.getChildByName("image");
							
							final String tileSource = imageElement.getAttribute("source");
							final String regionLookupName = Gdx.files.absolute(tileSource).nameWithoutExtension();
							
							// Create and add the tile.
							Tile tile = new Tile(regionLookupName, decoration);
							tileset[i] = tile;
							
							break;
						}
					}
				}
			}
		}
		
		final SubMap.Layer[] layers;
		final MapLocation[] tunnels;
		
		// Parse layers
		int mainLayerIndex = -1;
		{
			final Array<Element> layerElements = new Array<Element>();

			int layerCount = 0;
			
			{
				final int n = xmlRoot.getChildCount();
				
				for(int i = 0; i < n; i += 1) {
					final Element element = xmlRoot.getChild(i);
					
					if(element.getName().equals("layer")) {
						// Determine which layer count needs to be incremented(if main layer then none because it has only one tile layer).
						layerCount += 1;
						layerElements.add(element);
					}
					else if(element.getName().equals("objectgroup")) {
						// If the first back layer started not with tiles we still have to count this as a layer.
						if(layerCount == 0) {
							throw new RuntimeException("layers must start with tileset");
						}
						
						layerElements.add(element);
					}
				}
			}
			
			layers = new SubMap.Layer[layerCount];
			
			// Parse layers.
			{
				final Array<MapLocation> localTunnels = new Array<MapLocation>();
				int layersCreated = 0;
				
				{
					final int n = layerElements.size;
					for(int i = 0; i < n; i += 1) {
						final Element element = layerElements.get(i);
						
						if(element.getName().equals("layer")) {
							final byte[] tiles = parsePartitionTiles(partitionWidth, 
																	 partitionHeight, 
																	 element.getChildByName("data"), 
																	 layersCreated == 0 ? tileset : null);
							
							layers[layersCreated] = new SubMap.Layer(tiles, null);
							
							if(element.getAttribute("name").equalsIgnoreCase("Main")) {
								mainLayerIndex = layersCreated;
							}
							
							layersCreated += 1;
						}
						// If object group check for tunnels and static decorations
						else if(element.getName().equals("objectgroup")) {
							final int nn = element.getChildCount();
							
							final Array<StaticDecoration> sds = new Array<StaticDecoration>(StaticDecoration.class); 
							
							for(int ii = 0; ii < nn; ii += 1) {
								final Element objectElement = element.getChild(ii);
								if(objectElement.getAttributes().containsKey("type")) {
									if(objectElement.getAttribute("type").equalsIgnoreCase("location")) {
										final float objectX = objectElement.getFloatAttribute("x");
										final float objectY = objectElement.getFloatAttribute("y");
										final float objectWidth = objectElement.getFloatAttribute("width");
										final float objectHeight = objectElement.getFloatAttribute("height");
										final String objectName = objectElement.getAttribute("name");
										
										final MapLocation tunnel = new MapLocation(
												objectName, 
												// The y coordinate needs to be reversed(it's 0 is in the top) 
												objectX, heightInPixels - (objectY + objectHeight), 
												objectWidth,
												objectHeight);
										
										localTunnels.add(tunnel);
									}
								}
								else {
									sds.add(new StaticDecoration(
											objectElement.getFloatAttribute("x"),
											heightInPixels - objectElement.getFloatAttribute("y"),
											0,
											0,
											objectElement.getIntAttribute("gid")));
								}
							}
							layers[layersCreated - 1].decorations = sds.shrink();
						}
					}
				}
				
				tunnels = new MapLocation[localTunnels.size];
				
				System.out.println(tunnels.length + " locations found");
				System.out.println(layersCreated + " layers created");
				
				// Copy tunnels to fixed array.
				{
					final int n = localTunnels.size;
					for(int i = 0; i < n; i += 1) {
						tunnels[i] = localTunnels.get(i);
					}
				}
			}
		}
		
		if(mainLayerIndex == -1) {
			throw new IllegalArgumentException("no main layer");
		}
		
		return new SubMap(partitionWidth, partitionHeight, tileset, layers, mainLayerIndex, tunnels, tileWidth, tileHeight);
	}
	
	private static byte[] parsePartitionTiles(final int partitionWidth, 
											  final int partitionHeight,
											  final Element container,
											  final Tile[] tileset) {
		
		final byte[] tiles = new byte[partitionWidth * partitionHeight];
		final int n = container.getChildCount();
		
		for(int i = 0; i < n; i += 1) {
			// Convert to our indexing(tiled uses top left, we use bottom left as the first element).
			final int x = i % partitionWidth;
			final int y = partitionHeight - (i / partitionWidth) - 1;
			
			final int gid = container.getChild(i).getIntAttribute("gid");
			final int tileIndex = y * partitionWidth + x;
			
			if(gid == 0 && tileset != null) {
				tiles[tileIndex] = (byte)-128;//(-MathUtils.random(1, tileset.length - 1));
			}
			else {
				tiles[tileIndex] = (byte)(gid - 1 - 127);
			}
		}
		
		return tiles;
	}
	
}
