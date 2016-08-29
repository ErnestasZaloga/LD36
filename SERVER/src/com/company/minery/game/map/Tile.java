package com.company.minery.game.map;

public final class Tile {
	
	public final boolean decoration;
	
	public float width;
	public float height;
	
	public Tile(final String regionLookupName,
				final boolean decoration) {
		
		if(regionLookupName == null) {
			throw new IllegalArgumentException("regionLookupName cannot be null");
		}
		
		this.decoration = decoration;
	}
	
}
