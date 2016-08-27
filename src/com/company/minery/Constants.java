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
	public static final AssetResolution EDITOR_RESOLUTION = new AssetResolution(8737, 5460, "big");
	
	public static final AssetResolution[] RESOLUTION_LIST = new AssetResolution[] {
		RESOLUTION_XHDPI,
		RESOLUTION_FULLHD,
		RESOLUTION_HD,
		RESOLUTION_DEBUG
	};
	
	// *************************
	// NETWORKING
	// *************************
	public static final int DEFAULT_TCP_PORT = 54555;
	public static final int DEFAULT_UDP_PORT = 54777;

}