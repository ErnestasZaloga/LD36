package com.company.minery.game.player;

import com.company.minery.game.GameAssets.TextureRegionExt;

public class Node {

	public float offsetX;
	public float offsetY;
	public final float defaultOffsetX;
	public final float defaultOffsetY;
	public float width;
	public float height;
	public float rotation;
	public TextureRegionExt texture;
	
	public Node(float offsetX, float offsetY, float width, float height, float rotation, TextureRegionExt texture) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.defaultOffsetX = offsetX;
		this.defaultOffsetY = offsetY;
		this.width = width;
		this.height = height;
		this.rotation = rotation;
		this.texture = texture;
	}
	
}
