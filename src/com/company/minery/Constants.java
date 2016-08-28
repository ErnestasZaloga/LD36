package com.company.minery;

import com.company.minery.utils.AssetResolution;

public final class Constants {
	
	// *************************
	// RESOLUTIONS
	// *************************
	public static final AssetResolution RESOLUTION_XHDPI = new AssetResolution(2560, 1600, "xhdpi");
	public static final AssetResolution RESOLUTION_FULLHD = new AssetResolution(1920, 1080, "fullhd");
	public static final AssetResolution RESOLUTION_HD = new AssetResolution(1280, 800, "hd");
	public static final AssetResolution RESOLUTION_DEBUG = new AssetResolution(800, 600, "debug");
	
	public static final AssetResolution[] RESOLUTION_LIST = new AssetResolution[] {
		RESOLUTION_XHDPI,
		RESOLUTION_FULLHD,
		RESOLUTION_HD,
		RESOLUTION_DEBUG
	};
	
	// *************************
	// GAME
	// *************************
	public static final float CAMERA_FOLLOW_SPEED = 0.2f; // Of current delta
	public static final float MAX_FALL_SPEED = 35f; // Counted in tiles
	public static final float GRAVITY = 35f; // Counted in tiles
	public static final float JUMP_HEIGHT = 15f; // Counted in tiles
	public static final float RUN_SPEED = 10f; // Counted in tiles
	public static final float PIXELART_SCALE = 3f;
	
	public static final float SPEAR_HANDLE_MOD = 0.5f; // Counted in spear width
	public static final float SPEAR_TIP_MOD = 0.25f; // Counter in spear height
	
	// *************************
	// NETWORKING
	// *************************
	public static final int DEFAULT_TCP_PORT = 54555;
	public static final int DEFAULT_UDP_PORT = 54777;

}