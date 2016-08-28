package com.company.minery.game.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.company.minery.game.Game;
import com.company.minery.game.map.Map;

public final class InputTranslator {

	public interface PlayerInputListener {
		public void onLeftPressed();
		public void onRightPressed();
		public void onJumpPressed();
		public void onIdle();
		public void onAttackPressed(final float dirX, final float dirY);
	}

	private final Game game;
	
	private PlayerInputListener listener;
	
	private int playerLeft;
	private int playerRight;
	private int playerJump;

	public InputTranslator(final Game game) {
		this.game = game;
	}
	
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
		if(Gdx.input.justTouched()) {
			final float screenX = Gdx.input.getX();
			final float screenY = Gdx.graphics.getHeight() - Gdx.input.getY();
			final Map currentMap = game.currentMap();
			
			listener.onAttackPressed(screenX + currentMap.viewX, screenY + currentMap.viewY);
		}
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
