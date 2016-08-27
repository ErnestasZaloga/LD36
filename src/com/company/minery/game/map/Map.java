package com.company.minery.game.map;

import com.badlogic.gdx.utils.Array;
import com.company.minery.game.pawn.Pawn;
import com.company.minery.game.takable.Takable;

public final class Map {
	
	public float viewX; /**/ public float viewX() { return viewX; }
	public float viewY; /**/ public float viewY() { return viewY; }
	
	private float tileWidth; /**/ public float tileWidth() { return tileWidth; }
	private float tileHeight; /**/ public float tileHeight() { return tileHeight; }

	public final MapAssetLoader assetLoader;
	
	public final Layer[] layers;
	public final Layer mainLayer;
	public final int mainLayerIndex;
	
	public final Tunnel[] tunnels;
	
	public final Array<Pawn> pawns = new Array<Pawn>();
	public final Array<Takable> takables = new Array<Takable>();
	
	protected final Array<Tile[]> tileSets = new Array<Tile[]>();
	protected final Array<StaticDecoration[]> decorationSets = new Array<StaticDecoration[]>();
	
	public Map(final MapAssetLoader assetLoader,
			   final float tileWidth, 
			   final float tileHeight, 
			   final Layer[] layers,
			   final Layer mainLayer,
			   final int mainLayerIndex,
			   final Tunnel[] tunnels) {
		
		this.assetLoader = assetLoader;
		this.layers = layers;
		this.mainLayer = mainLayer;
		this.mainLayerIndex = mainLayerIndex;
		this.tunnels = tunnels;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		
		for(final Layer layer : layers) {
			final Tile[] tileset = layer.tiles.tileset;
			if(!tileSets.contains(tileset, true)) {
				tileSets.add(tileset);
			}
			
			final StaticDecoration[] decorSet = layer.decorations();
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
			final Tunnel[] tunnels = this.tunnels;
			final int n = tunnels.length;
			
			for(int i = 0; i < n; i += 1) {
				tunnels[i].setScale(rescale);
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
						tile.setWidth(tile.width() * rescale);
						tile.setHeight(tile.height() * rescale);
					}
				}
			}
		}
		
		// Rescale pawns
		{
			final Array<Pawn> pawns = this.pawns;
			final int n = pawns.size;
			
			for(int i = 0; i < n; i += 1) {
				final Pawn pawn = pawns.get(i);
				
				pawn.x = pawn.x * rescale;
				pawn.y = pawn.y * rescale;
				pawn.velocityX = pawn.velocityX * rescale;
				pawn.velocityY = pawn.velocityY * rescale;
			}
		}
	}
	
	public Tunnel findTunnelByName(final String tunnelName) {
		final Tunnel[] tunnels = this.tunnels;
		final int n = tunnels.length;
		
		for(int i = 0; i < n; i += 1) {
			final Tunnel tunnel = tunnels[i];
			
			if(tunnel.name.equalsIgnoreCase(tunnelName)) {
				return tunnel;
			}
		}
		
		return null;
	}
	
}
