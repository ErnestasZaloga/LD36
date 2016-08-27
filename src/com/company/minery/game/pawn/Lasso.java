package com.company.minery.game.pawn;


public final class Lasso {

	public float startX;
	public float startY;
	public float endX;
	public float endY;
	public float velocityX;
	public float velocityY;
	public boolean enabled;
	public boolean hooked;
	public float length;
	public float angle;
	
	public void calculateLength() {
		final float dstX = endX - startX;
		final float dstY = endY - startY;
		length = (float)Math.sqrt(dstX * dstX + dstY * dstY);
	}
	
}
