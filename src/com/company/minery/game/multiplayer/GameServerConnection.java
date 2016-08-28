package com.company.minery.game.multiplayer;

import com.company.minery.game.player.Player;
import com.company.minery.utils.kryonet.Connection;

public final class GameServerConnection extends Connection {

	public final Player player;
	private long impulseTimeThreshold; /**/ public long impulseTimeThreshold() { return impulseTimeThreshold; }

	public GameServerConnection(final Player player) {
		this.player = player;
	}
	
	public void setImpulseTimeThreshold(final long impulseTimeThreshold) {
		this.impulseTimeThreshold = impulseTimeThreshold;
	}
	
}