package com.company.minery.game.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.company.minery.game.GameAssets;

public final class Generator {
	
	private static final class SubMap {
		
		private static final class Layer {
			public final byte[] tiles;
			protected StaticDecoration[] decorations;
		
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
		public final Tunnel[] tunnels;
		
		public final float tileWidth;
		public final float tileHeight;
		
		public SubMap(final int width, 
					  final int height, 
					  final Tile[] tileset, 
					  final SubMap.Layer[] layers,
					  final int mainLayerIndex,
					  final Tunnel[] tunnels,
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
	
	private static final MapAssetLoader testMapAssetReloader = new MapAssetLoader() {
		@Override
		public void load(final Map map, 
						 final GameAssets assets) {

			final TextureAtlas[] atlases = new TextureAtlas[] {
					assets.tilesAtlas(),
					assets.decalsAtlas()
			};

			final Layer[] layers = map.layers;
			final int n = layers.length;
			
			for(int i = 0; i < n; i += 1) {
				final Layer layer = layers[i];
				final Tile[] tileset = layer.tiles.tileset;
				
				for(int ii = 0, nn = tileset.length; ii < nn; ii += 1) {
					// Find region
					TextureRegion region = null;
					for(int iii = 0, nnn = atlases.length; iii < nnn; iii += 1) {
						region = atlases[iii].findRegion(tileset[ii].regionLookupName);
						
						if(region != null) {
							break;
						}
					}
					
					if(region == null) {
						throw new RuntimeException("Tile named " + tileset[ii].regionLookupName + " not found in given atlases");
					}
					
					tileset[ii].setRegion(region);
				}
				
				final StaticDecoration[] decorations = layer.decorations();
				
				if(decorations == null) {
					continue;
				}
				
				for(int ii = 0, nn = decorations.length; ii < nn; ii++) {
					final StaticDecoration deco = decorations[ii];
					final Tile tile = tileset[deco.gid - 1];
					TextureRegion region = tile.region();
					deco.setRegion(region);
					deco.setWidth(tile.width());
					deco.setHeight(tile.height());
				}
			}
		}
	};
	
	public static Map generateTestMap(final GameAssets assets) {
		final SubMap testPartition = createPartition(assets.testMapXml);
		
		final Layer[] layers = convertLayers(testPartition, testPartition.layers, 0, 0);
		final Layer mainLayer = layers[testPartition.mainLayerIndex];
		
		//Put minable and non-minable tiles into two arrays
		final Tile[] tileset = mainLayer.tiles.tileset;
		final IntArray minableIndexes = new IntArray();
		final IntArray nonMinableIndexes = new IntArray();
		for(int i = 0, n = tileset.length; i < n; i++) {
			final Tile tile = tileset[i];
			
			if(tile.decoration) {
				continue;
			}
			
			if(tile.minable) {
				minableIndexes.add(i);
			}
			else {
				nonMinableIndexes.add(i);
			}
		}
		
		final int x = mainLayer.tiles.width / 2;
		final int y = mainLayer.tiles.height / 2;
		final int mainLayerID = testPartition.mainLayerIndex;
		
		for(int i = 0, n = layers.length; i < n; i++) {
			
			final Layer layer = layers[i];
			
			if(i == mainLayerID) {
				layers[i] = enlargeLayer(layer,
										 true,
										 minableIndexes,
										 nonMinableIndexes,
										 layer.tiles.width * 4, 
										 layer.tiles.height * 4,
										 x,
										 y,
										 0,
										 0);
			} 
			else {
				layers[i] = enlargeLayer(layer,
										 false,
										 minableIndexes,
										 nonMinableIndexes,
										 layer.tiles.width, 
										 layer.tiles.height,
										 0,
										 0,
										 x,
										 y);
			}
		}
		
		final Map subMap = new Map(testMapAssetReloader,
								   testPartition.tileWidth,
								   testPartition.tileHeight,
								   layers,
								   layers[mainLayerID],
								   testPartition.mainLayerIndex,
								   testPartition.tunnels);
		
		subMap.assetLoader.load(subMap, assets);
		
		//reposition tunnels and static decorations
		final float reposX = subMap.tileWidth() * x;
		final float reposY = subMap.tileHeight() * y;
		
		final Tunnel[] tunnels = subMap.tunnels;
		for(final Tunnel tunnel : tunnels) {
			tunnel.x += reposX;
			tunnel.y += reposY;
		}
		
		for(Layer layer : layers) {
			if(layer.decorations() == null) { 
				continue; 
			}
			
			for(StaticDecoration deco : layer.decorations()) {
				deco.setX(deco.x() + reposX);
				deco.setY(deco.y() + reposY);
			}
		}
		
		return subMap;
	}
	
	private static Layer enlargeLayer(final Layer src, 
									  final boolean enclose,
									  final IntArray minableIndexes,
									  final IntArray nonMinableIndexes,
									  final int newWidth, 
									  final int newHeight,
									  final int srcLayerX,
									  final int srcLayerY,
									  final int thisLayerX,
									  final int thisLayerY) {
		
		final Tiles srcTiles = src.tiles;
		
		final int width = srcTiles.width;
		final int height = srcTiles.height;
		
		// If no resizing needed return source layer with changed position
		if(srcTiles.width == newWidth && srcTiles.height == newHeight) {
			srcTiles.setTileXCoord(thisLayerX);
			srcTiles.setTileYCoord(thisLayerY);
			return src;
		}
		
		final byte[] srcTileIDs = srcTiles.tiles;
		final Tile[] srcTileSet = src.tiles.tileset;
		
		final int srcLayerRight = srcLayerX + width;
		final int srcLayerTop = srcLayerY + height;
		
		final byte[] newTileIDs = new byte[newWidth * newHeight];
		
		// Routine for imprinting all source layer tiles into a 
		// larger array in given position (sourceLayerX, sourceLayerY)
		final int tmpWidth = newWidth - 1;
		final int tmpHeight = newHeight - 1;
		
		for(int i = 0, n = newTileIDs.length; i < n ; i++) {
			
			final int x = i % newWidth;
			final int y = i / newWidth;
			
			if(x >= srcLayerX 
			&& x < srcLayerRight 
			&& y >= srcLayerY 
			&& y < srcLayerTop) {
				
				newTileIDs[i] = srcTileIDs[(y - srcLayerY) * width + (x - srcLayerX)];
			} 
			else {
				if(enclose && (x == 0 || y == 0 || x == tmpWidth || y == tmpHeight)) {
					newTileIDs[i] = ((byte) (nonMinableIndexes.random() - 127));
				}
				else {
					newTileIDs[i] = ((byte) (minableIndexes.random() - 127));
				}
			}
		}
		
		final Tiles tmpTiles = new Tiles(newTileIDs,
										 srcTileSet,
										 thisLayerX,
										 thisLayerY,
										 newWidth,
										 newHeight);
		
		return new Layer(tmpTiles, src.decorations());
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
		System.out.println("Partition height in pixels: " + heightInPixels);
		
		final Tile[] tileset;
		
		// Parse tile set
		{
			final Array<String> tilesetsTypes = new Array<String>();
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
					for(int ii = 1; ii < nn; ii += 1) {
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
					final int gid = i + 1;
					
					for(int ii = 0; ii < n; ii += 1) {
						if(tileElementGids.get(ii) == gid) {
							final Element tileElement = tileElements.get(ii);
							
							boolean minable = false;
							boolean decoration = false;
							
							final Element tilePropertiesElement = tileElement.getChildByName("properties");
							if(tilePropertiesElement != null) {
								final int nn = tilePropertiesElement.getChildCount();
								for(int iii = 0; iii < nn; iii += 1) {
									final Element tilePropertyElement = tilePropertiesElement.getChild(iii);
									
									if(tilePropertyElement.getAttribute("name").equalsIgnoreCase("minable") &&
									    Boolean.valueOf(tilePropertyElement.getAttribute("value"))) {
										
										minable = true;
									}
									
									if(tilePropertyElement.getAttribute("name").equalsIgnoreCase("decoration") &&
									   Boolean.valueOf(tilePropertyElement.getAttribute("value"))) {
												
									   decoration = true;
									}
								}
							}
							
							// Build region lookup name
							System.out.println(tileElement.getChildByName("image"));
							if(tileElement.getChildByName("image") == null) {
								System.out.println("ffffffffffffffffg");
								continue;
							}
							final Element imageElement = tileElement.getChildByName("image");
							
							final String tileSource = imageElement.getAttribute("source");
							final String regionLookupName = Gdx.files.absolute(tileSource).nameWithoutExtension();
							
							// Create and add the tile
							
							final float tileW = Float.parseFloat(imageElement.getAttribute("width"));
							final float tileH = Float.parseFloat(imageElement.getAttribute("height"));
						
							Tile tile = new Tile(regionLookupName, minable, decoration);
							tileset[i] = tile;
							
							break;
						}
					}
				}
				for(final Tile tile : tileset) {
					System.out.println(tile == null);
				}
			}
		}
		
		final SubMap.Layer[] layers;
		final Tunnel[] tunnels;
		
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
			
			// Parse layers
			{
				final Array<Tunnel> localTunnels = new Array<Tunnel>();
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
							
							final Element elementProperties = element.getChildByName("properties");
							if(elementProperties != null) {
								for(int ii = 0; ii < elementProperties.getChildCount(); ii++) {
									if(elementProperties.getChild(ii).getAttribute("name").equalsIgnoreCase("Main")) {
										mainLayerIndex = layersCreated;
									}
								}
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
									if(objectElement.getAttribute("type").equalsIgnoreCase("tunnel")) {
										final float objectX = objectElement.getFloatAttribute("x");
										final float objectY = objectElement.getFloatAttribute("y");
										final float objectWidth = objectElement.getFloatAttribute("width");
										final float objectHeight = objectElement.getFloatAttribute("height");
										final String objectName = objectElement.getAttribute("name");
										
										final Tunnel tunnel = new Tunnel(
												objectName, 
												// The y coordinate needs to be reversed(it's 0 is in the top) 
												objectX, heightInPixels - (objectY + objectHeight), 
												objectWidth,
												objectHeight);
										
										localTunnels.add(tunnel);
									}
								} else {
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
				
				tunnels = new Tunnel[localTunnels.size];
				System.out.println(tunnels.length + " tunnels found");
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
