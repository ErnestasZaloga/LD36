package com.company.minery.utils;

import com.badlogic.gdx.Gdx;
import com.company.minery.Constants;

public class AssetResolution {
	
	public float calcScale() {
		final int height = Gdx.graphics.getHeight();
		final int tileSize = Constants.TILE_SIZE;
		final int tilesInHeight = Constants.TILES_IN_HEIGHT;
		
		final float currentTiles = (float) height / tileSize;
		
		System.out.println("Height: " + height + " curr tiles: " + currentTiles);
		
		return currentTiles / tilesInHeight;
	}
	
}
