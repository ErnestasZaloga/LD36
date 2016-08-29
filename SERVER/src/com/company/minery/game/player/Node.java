package com.company.minery.game.player;

import com.company.minery.game.GameAssets.TextureRegionExt;

public class Node {

	public float offsetX;
	public float offsetY;
	public float rotation;
	public TextureRegionExt texture;
	public float originX;
	public float originY;
	
	public final void flip(final float center) {
		originX = texture.getWidth() - originX;
		offsetX += 2 * -(offsetX + originX - center);
		
		normalizeRotation();
		
			rotation = 360 - rotation;

		normalizeRotation();
	}
	
	private void normalizeRotation() {
		while(rotation < 0) {
			rotation += 360f;
		}
		
		rotation %= 360;
	}
	
}
