package com.company.minery.game.player;

import com.company.minery.Constants;
import com.company.minery.game.GameAssets;
import com.company.minery.game.GameAssets.TextureRegionExt;

public final class Spear extends PhysicalObject {
	
	public TextureRegionExt region;
	public float lastRotation;
	
	public Spear() {
	}
	
	public Spear(final long uid) {
		super(uid);
	}

	@Override
	public void applyAppearance(final GameAssets assets) {
		region = assets.spear;
		width = assets.spear.getHeight();
		height = assets.spear.getHeight();
	}
	
}
