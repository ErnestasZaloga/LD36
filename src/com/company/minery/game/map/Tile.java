package com.company.minery.game.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Tile {
	
	public final boolean minable;
	public final boolean decoration;
	public final String regionLookupName;
	
	private TextureRegion region; /**/ public final TextureRegion region() { return region; }
	
	private float width; /**/ public final float width() { return width; }
	private float height; /**/ public final float height() { return height; }
	
	
	public Tile(final String regionLookupName,
				final boolean minable,
				final boolean decoration) {
		
		this.regionLookupName = regionLookupName;
		this.minable = minable;
		this.decoration = decoration;
	}
	
	public void setRegion(final TextureRegion region) {
		this.region = region;
	}

	protected final void setWidth(float width) {
		this.width = width;
	}
	
	protected final void setHeight(float height) {
		this.height = height;
	}
}
