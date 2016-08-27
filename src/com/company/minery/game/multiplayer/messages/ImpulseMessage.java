package com.company.minery.game.multiplayer.messages;

public final class ImpulseMessage extends BaseUdpMessage {
	public static final byte FLAG_MOVE_IDLE = 0;
	public static final byte FLAG_MOVE_LEFT = 1;
	public static final byte FLAG_MOVE_RIGHT = 2;
	
	public byte movementFlag;
	public boolean jumpFlag;
}
