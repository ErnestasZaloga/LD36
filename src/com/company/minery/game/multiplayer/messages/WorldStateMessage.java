package com.company.minery.game.multiplayer.messages;

public final class WorldStateMessage extends BaseUdpMessage {
	public PawnMessage[] pawns;
	public byte resolutionIndex;
}
