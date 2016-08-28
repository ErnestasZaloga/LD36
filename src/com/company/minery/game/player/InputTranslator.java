package com.company.minery.game.player;

import com.badlogic.gdx.Gdx;

public final class InputTranslator {

	public interface PlayerInputListener {
		public void onLeftPressed();
		public void onRightPressed();
		public void onJumpPressed();
		public void onIdle();
		public void onAttackPressed(final float dirX, final float dirY);
	}

	private PlayerInputListener listener;
	
	private int playerLeft;
	private int playerRight;
	private int playerJump;
	
	public void setListener(final PlayerInputListener listener) {
		this.listener = listener;
	}

	public void setMovementKeys(final int left,
								final int right,
								final int jump) {
		
		this.playerLeft = left;
		this.playerRight = right;
		this.playerJump = jump;
	}
	
	public void update() {
		if(Gdx.input.isKeyPressed(playerLeft)) {
			listener.onLeftPressed();
		}
		else if(Gdx.input.isKeyPressed(playerRight)) {
			listener.onRightPressed();
		}
		else {
			listener.onIdle();
		}
		
		if(Gdx.input.isKeyJustPressed(playerJump)) {
			listener.onJumpPressed();
		}
	}
	
}
