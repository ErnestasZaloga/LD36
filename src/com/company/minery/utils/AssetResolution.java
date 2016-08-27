package com.company.minery.utils;

import com.badlogic.gdx.Gdx;

public class AssetResolution {
	
	//TODO: remove on release;
	public static float ZOOM = 1f;
	
	public final int width;
	public final int height;
	public final String name;
	
	public AssetResolution(final int width, 
						   final int height,
						   final String name) {
		
		if(name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		
		this.width = width;
		this.height = height;
		this.name = name;
	}
	
	public float calcScale() {
		final int width = Gdx.graphics.getWidth();
		final int height = Gdx.graphics.getHeight();
		
		return calcRequiredSizeScale(width > height ? height : width) / ZOOM;
	}
	
	/**
	 * Calculates how much the given size needs to be scaled to fit in given resolution.
	 * */
	public float calcRequiredSizeScale(final int size) {
		return (float)size / (float)pickSmallerDimension();
	}

	/**
	 * Calculates how much the resolution needs to be scaled to fit in given size.
	 * */
	public float calcRequiredResolutionScale(final int size) {
		return (float)pickSmallerDimension() / (float)size;
	}
	
	public float calcWidthScale() {
		return (float)Gdx.graphics.getWidth() / (float)width;
	}
	
	public float calcHeightScale() {
		return (float)Gdx.graphics.getHeight() / (float)height;
	}
	
	private int pickSmallerDimension() {
		return height > width ? width : height;
	}
	
}
