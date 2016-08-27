package com.company.minery.game.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class StaticDecoration {
	
	private float x; /**/ public final float x() { return x; }
	private float y; /**/ public final float y() { return y; }
	private float width; /**/ public final float width() { return width; }
	private float height; /**/ public final float height() { return height; }
	
	private TextureRegion region; /**/ public final TextureRegion region() { return region; }
	public final int gid;
	
	public StaticDecoration(final float x, 
					  		final float y, 
					  		final float width, 
					  		final float height,
					  		final int gid) {
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gid = gid;
	}
	
	protected void setRegion(final TextureRegion region) {
		this.region = region;
	}
	
	public void setScale(final float rescale) {
		this.x *= rescale;
		this.y *= rescale;
		this.width *= rescale;
		this.height *= rescale;
	}

	protected void setX(final float x) {
		this.x = x;
	}

	protected void setY(final float y) {
		this.y = y;
	}

	protected void setWidth(final float width) {
		this.width = width;
	}

	protected void setHeight(final float height) {
		this.height = height;
	}
	
}
