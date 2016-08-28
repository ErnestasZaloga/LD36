package com.company.minery;

public final class Constants {
	
	// *************************
	// GAME
	// *************************
	public static final float CAMERA_FOLLOW_SPEED = 0.2f; // Of current delta
	public static final float MAX_FALL_SPEED = 35f; // Counted in tiles
	public static final float GRAVITY = 35f; // Counted in tiles
	public static final float JUMP_HEIGHT = 15f; // Counted in tiles
	public static final float RUN_SPEED = 10f; // Counted in tiles
	public static final float PIXELART_SCALE = 1f;
	
	public static final float SPEAR_HANDLE_MOD = 0.5f; // Counted in spear width
	public static final float SPEAR_TIP_MOD = 0.25f; // Counter in spear height
	
	public static final int TILE_SIZE = 16; // Calculated in pixels;
	public static final int TILES_IN_HEIGHT = 20;
	
	public static final float IDLE_ANIMATION_DURATION = 0.5f;
	public static final float RUN_ANIMATION_DURATION = 0.2f;
	
	public static final int HEADSHOT_DAMAGE = 2;
	public static final int DAMAGE = 1;
	
	public static final float HIT_VELOCITY = 1.5f; // Counted in max velocity
	
	public static final int LIVES = 2;
	
	// *************************
	// NETWORKING
	// *************************
	public static final int DEFAULT_TCP_PORT = 54555;
	public static final int DEFAULT_UDP_PORT = 54777;

}