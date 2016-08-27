package com.company.minery.game.pawn.ai;

import com.badlogic.gdx.utils.Array;

public final class Waypoint {
	public static enum ConnectionType {
		Walk,
		Fall,
		Jump;
	}
	
	public int x;
	public int y;
	public final Array<Waypoint> connections = new Array<Waypoint>(5);
	public final Array<ConnectionType> connectionTypes = new Array<ConnectionType>(5);
	public int runId;
	public boolean ground;
}
