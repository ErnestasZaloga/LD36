package com.company.minery.game.multiplayer.messages;

public final class ImpulseMessage extends BaseMessage {
	
	public static final byte FLAG_MOVE_IDLE = 0;
	public static final byte FLAG_MOVE_LEFT = 1;
	public static final byte FLAG_MOVE_RIGHT = 2;
	
	public byte movementFlag;
	public boolean jumpFlag;
	public boolean attackFlag;
	public float attackX;
	public float attackY;
	public float scale;
	
}