package com.company.minery.game.player;

import com.company.minery.game.GameAssets;
import com.company.minery.game.player.Player.MovementDirection;

public abstract class PhysicalObject {
	
	private static long uidCounter = Long.MIN_VALUE;
	
	public final long uid;
	public MovementDirection movementDirection = MovementDirection.Idle;
	
	public float x;
	public float y;
	public float width;
	public float height;
	public float velocityX;
	public float velocityY;
	
	public boolean requestsJump;
	public boolean isInAir;
	public boolean isJumping;
	public boolean isRunning;
	
	public float animationTimer;
	
	public PhysicalObject() {
		this.uid = uidCounter++;
	}
	
	public PhysicalObject(final long uid) {
		this.uid = uid;
	}
	
	public abstract void applyAppearance(final GameAssets assets);
	
}