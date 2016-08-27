package com.company.minery.game.pawn.input;

import com.badlogic.gdx.Gdx;
import com.company.minery.game.Game;
import com.company.minery.game.map.Map;

public final class InputTranslator {

	public interface PlayerInputListener {
		public void onMiningPointed(final float mapX, 
								    final float mapY);
		
		public void onLassoPointed(final float mapX, 
								   final float mapY);
		
		public void onLeftPressed();
		public void onRightPressed();
		public void onJumpPressed();
		public void onIdle();
	}

	private final Game game;
	private PlayerInputListener listener; /**/ public PlayerInputListener listener() { return listener; }
	
	private int miningPointingButton;
	private int launchLassoButton;
	
	private int playerLeft;
	private int playerRight;
	private int playerJump;
	
	public InputTranslator(final Game game) {
		this.game = game;
	}
	
	public void setListener(final PlayerInputListener listener) {
		this.listener = listener;
	}

	public void setMouseControls(final int miningPointingButton,
								 final int launchLassoButton) {
		
		this.miningPointingButton = miningPointingButton;
		this.launchLassoButton = launchLassoButton;
	}
	
	public void setMovementKeys(final int left,
								final int right,
								final int jump) {
		
		this.playerLeft = left;
		this.playerRight = right;
		this.playerJump = jump;
	}
	
	public void update() {
		if(Gdx.input.isButtonPressed(miningPointingButton)) {
			final float screenX = Gdx.input.getX();
			final float screenY = Gdx.graphics.getHeight() - Gdx.input.getY();
			final Map currentMap = game.currentMap();
			
			listener.onMiningPointed(screenX + currentMap.viewX, screenY + currentMap.viewY);
		}
		
		if(Gdx.input.justTouched() && Gdx.input.isButtonPressed(launchLassoButton)) {
			final float screenX = Gdx.input.getX();
			final float screenY = Gdx.graphics.getHeight() - Gdx.input.getY();
			final Map currentMap = game.currentMap();
			
			listener.onLassoPointed(screenX + currentMap.viewX, screenY + currentMap.viewY);
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
