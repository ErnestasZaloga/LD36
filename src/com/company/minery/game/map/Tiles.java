package com.company.minery.game.map;

public final class Tiles {
	public final byte[] tiles;
	public final Tile[] tileset;
	private int tileXCoord; /**/ public final int tileXCoord() { return tileXCoord; }
	private int tileYCoord; /**/ public final int tileYCoord() { return tileYCoord; }
	public final int width;
	public final int height;
	
	public Tiles(final byte[] tiles,
				 final Tile[] tileset,
				 final int tileXCoord,
				 final int tileYCoord,
				 final int width,
				 final int height) {

		this.tiles = tiles;
		this.tileset = tileset;
		this.tileXCoord = tileXCoord;
		this.tileYCoord = tileYCoord;
		this.width = width;
		this.height = height;
	}

	public void setTileXCoord(int tileXCoord) {
		this.tileXCoord = tileXCoord;
	}

	public void setTileYCoord(int tileYCoord) {
		this.tileYCoord = tileYCoord;
	}
}
