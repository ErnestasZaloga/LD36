package com.company.minery.game.map;

public final class Tiles {
	
	// Index to a tile in tileset.
	public final byte[] tiles;
	
	// List of tile resources.
	public final Tile[] tileset;
	
	public int tileXCoord;
	public int tileYCoord;
	public final int width;
	public final int height;
	
	public Tiles(final byte[] tiles,
				 final Tile[] tileset,
				 final int tileXCoord,
				 final int tileYCoord,
				 final int width,
				 final int height) {
		
		if(tileset == null) {
			throw new RuntimeException("Tileset cannot be null");
		}

		this.tiles = tiles;
		this.tileset = tileset;
		this.tileXCoord = tileXCoord;
		this.tileYCoord = tileYCoord;
		this.width = width;
		this.height = height;
	}

}