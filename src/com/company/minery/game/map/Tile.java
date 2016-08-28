package com.company.minery.game.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Tile {
	
	public final boolean decoration;
	public final String regionLookupName;
	
	public TextureRegion region;
	public float width;
	public float height;
	
	public Tile(final String regionLookupName,
				final boolean decoration) {
		
		if(regionLookupName == null) {
			throw new IllegalArgumentException("regionLookupName cannot be null");
		}
		
		this.regionLookupName = regionLookupName;
		this.decoration = decoration;
	}
	
}
