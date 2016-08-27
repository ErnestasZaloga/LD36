package com.company.minery.game.map;

public final class Tunnel {
	
	public final String name;
	public float x;
	public float y;
	public float width;
	public float height;
	
	// TODO: add mapping
	
	public Tunnel(final String name, 
				  final float x, 
				  final float y,
				  final float width,
				  final float height) {
		
		this.name = name;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void setScale(final float rescale) {
		this.x *= rescale;
		this.y *= rescale;
		this.width *= rescale;
		this.height *= rescale;
	}
}
