package com.company.minery.game.multiplayer.messages;

public final class WorldStateMessage extends BaseMessage {
	public PlayerMessage[] players;
	public SpearMessage[] spears;
	public byte resolutionIndex;
}
