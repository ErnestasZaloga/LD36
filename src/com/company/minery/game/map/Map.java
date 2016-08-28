package com.company.minery.game.map;

import com.badlogic.gdx.utils.Array;
import com.company.minery.game.player.PhysicalObject;

public final class Map {
	
	public float viewX;
	public float viewY;
	
	public float tileWidth;
	public float tileHeight;

	public final MapAssetLoader assetLoader;
	
	public final Layer[] layers;
	public final Layer mainLayer;
	public final int mainLayerIndex;
	
	public final MapLocation[] mapLocations;
	
	public final Array<PhysicalObject> physicalObjects = new Array<PhysicalObject>();
	
	protected final Array<Tile[]> tileSets = new Array<Tile[]>();
	protected final Array<StaticDecoration[]> decorationSets = new Array<StaticDecoration[]>();
	
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
			
			final StaticDecoration[] decorSet = layer.decorations;
			if(!decorationSets.contains(decorSet, true) && decorSet != null) {
				decorationSets.add(decorSet);
			}
		}
	}
	
	public void setViewPosition(final float viewX, 
								final float viewY) {
		
		this.viewX = viewX;
		this.viewY = viewY;
	}
	
	public void setScale(final float rescale) {
		tileWidth *= rescale;
		tileHeight *= rescale;

		// Rescale tunnels
		{
			final MapLocation[] mapLocations = this.mapLocations;
			final int n = mapLocations.length;
			
			for(int i = 0; i < n; i += 1) {
				mapLocations[i].setScale(rescale);
			}
		}
		
		// Rescale layers
		{
			final Layer[] layers = this.layers;
			final int n = layers.length;
			
			for(int i = 0; i < n; i += 1) {
				layers[i].setScale(rescale);
			}
		}
		
		//Rescale static decorations
		{
			for(final StaticDecoration[] decorSet : decorationSets) {
				for(final StaticDecoration deco : decorSet) {
					deco.setScale(rescale);
				}
			}
		}
		
		//Rescale Tilesets' decoration tiles
		{
			for(final Tile[] tileset : tileSets) {
				for(final Tile tile : tileset) {
					if(tile.decoration) {
						tile.width *= rescale;
						tile.height *= rescale;
					}
				}
			}
		}
		
		// Rescale pawns
		{
			final Array<PhysicalObject> physicalObjects = this.physicalObjects;
			final int n = physicalObjects.size;
			
			for(int i = 0; i < n; i += 1) {
				final PhysicalObject physicalObject = physicalObjects.get(i);
				
				physicalObject.x = physicalObject.x * rescale;
				physicalObject.y = physicalObject.y * rescale;
				physicalObject.velocityX = physicalObject.velocityX * rescale;
				physicalObject.velocityY = physicalObject.velocityY * rescale;
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
