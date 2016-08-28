package com.company.minery.game.multiplayer.messages;

public abstract class ObjectMessage {

	public long uid;
	
	public float x;
	public float y;
	
	public byte movementDirection;
	public float velocityX;
	public float velocityY;
	
	public boolean requestsJump;
	public boolean isJumping;
	
}
