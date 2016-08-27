package com.company.minery.game.multiplayer.messages;

public final class PawnMessage {
	public long pawnUid;
	
	public float x;
	public float y;
	
	public byte movementDirection;
	public float velocityX;
	public float velocityY;
	
	public boolean requestsJump;
	public boolean isJumping;
	
	public boolean requestsLasso;
	public float lassoTargetX;
	public float lassoTargetY;
	
	public boolean requestsMining;
	public float miningTargetX;
	public float miningTargetY;
	
	public float lassoStartX;
	public float lassoStartY;
	public float lassoEndX;
	public float lassoEndY;
	public float lassoVelocityX;
	public float lassoVelocityY;
	public boolean lassoEnabled;
	public boolean lassoHooked;
	
	public float animationTimer;
	public boolean skeletonFlipped;
}
