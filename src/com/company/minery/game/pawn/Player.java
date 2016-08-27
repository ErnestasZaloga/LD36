package com.company.minery.game.pawn;

import com.company.minery.game.pawn.input.InputTranslator;

public class Player extends Pawn implements InputTranslator.PlayerInputListener {
	
	public final boolean local;

	public Player(final boolean local) {
		this.local = local;
	}
	
	public Player(final boolean local, 
				  final long uid) {
		
		super(uid);
		this.local = local;
	}
	
	@Override
	public void onLeftPressed() {
		if(!pathfindingEnabled) {
			movementDirection = MovementDirection.Left;
		}
	}

	@Override
	public void onRightPressed() {
		if(!pathfindingEnabled) {
			movementDirection = MovementDirection.Right;
		}
	}

	@Override
	public void onJumpPressed() {
		if(!pathfindingEnabled) {
			requestsJump = true;
		}
	}
	
	@Override
	public void onIdle() {
		if(!pathfindingEnabled) {
			movementDirection = MovementDirection.Idle;
		}
	}

	@Override
	public void onMiningPointed(final float mapX, 
								final float mapY) {
		
		requestsMining = true;
		miningTargetX = mapX;
		miningTargetY = mapY;
	}

	@Override
	public void onLassoPointed(final float mapX, 
							   final float mapY) {
		
		pathfindingEnabled = true;
		initialPathFound = false;
		pathfindingMapX = mapX;
		pathfindingMapY = mapY;
	}

}
