package com.company.minery.game.takable;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RegionTakable extends Takable{
	
	private TextureRegion region; /**/ public final TextureRegion region() { return region; }

	public void setTakableRegion(final TextureRegion region) {
		this.region = region;
		
		setWidth(region.getRegionWidth());
		setHeight(region.getRegionHeight());
	}

	@Override
	public final void reset() {
		super.reset();
		region = null;
	}
}
