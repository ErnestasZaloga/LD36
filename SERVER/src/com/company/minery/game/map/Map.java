package com.company.minery.game.map;

import com.badlogic.gdx.utils.Array;

public final class Map {
	
	public float tileWidth;
	public float tileHeight;

	public final MapAssetLoader assetLoader;
	
	public final Layer[] layers;
	public final Layer mainLayer;
	public final int mainLayerIndex;
	
	public final MapLocation[] mapLocations;
	
	protected final Array<Tile[]> tileSets = new Array<Tile[]>();
	
	public Map(final MapAssetLoader assetLoader,
			   final float tileWidth, 
			   final float tileHeight, 
			   final Layer[] layers,
			   final Layer mainLayer,
			   final int mainLayerIndex,
			   final MapLocation[] mapLocations) {
		
		this.assetLoader = assetLoader;
		this.layers = layers;
		this.mainLayer = mainLayer;
		this.mainLayerIndex = mainLayerIndex;
		this.mapLocations = mapLocations;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		
		for(final Layer layer : layers) {
			final Tile[] tileset = layer.tiles.tileset;
			if(!tileSets.contains(tileset, true)) {
				tileSets.add(tileset);
			}
		}
	}
	
	public MapLocation findLocationByName(final String tunnelName) {
		final MapLocation[] mapLocations = this.mapLocations;
		final int n = mapLocations.length;
		
		for(int i = 0; i < n; i += 1) {
			final MapLocation location = mapLocations[i];
			
			if(location.name.equalsIgnoreCase(tunnelName)) {
				return location;
			}
		}
		
		return null;
	}
	
}
