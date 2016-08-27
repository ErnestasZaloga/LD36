package com.company.minery.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.company.minery.App;
import com.company.minery.game.Game;
import com.company.minery.game.GameListener;
import com.company.minery.game.GameRenderer;
import com.company.minery.gameui.GameUi;
import com.company.minery.gameui.GameUiListener;

public class GameScreen extends BaseScreen {
	
	private final GameUi gameUi;
	private final Game game;
	private final GameRenderer gameRenderer;
	private float updateTime;
	
	private final GameUiListener gameUiListener = new GameUiListener() {
		@Override
		public void onUserRequestedExit() {
			Gdx.app.exit();
		}

		@Override
		public void onUserRequestedPause() {
		}
		
		@Override
		public void onUserRequestedResume() {
		}
	};
	
	private final GameListener gameListener = new GameListener() {
		@Override
		public void onGameCompleted() {
		}

		@Override
		public void onGameLost() {
		}
	};
	
	public GameScreen(final App app) {
		super(app);
		
		backgroundColor.set(Color.valueOf("524f2c"));
		
		gameUi = new GameUi(gameUiListener, app.batch());
		game = new Game(gameUi, gameListener);
		gameRenderer = new GameRenderer(app.batch());
	}
	
	public void beginGame() {
		gameUi.begin();
		game.begin();
	}

	@Override
	public void onResize(final int newWidth, 
						 final int newHeight) {
		
		game.setSize(newWidth, newHeight, app.assetResolution());
	}
	
	@Override
	public void onUpdate(final float deltaTime) {
		final long startTime = System.nanoTime();
		
		game.update(deltaTime);
		gameUi.update(deltaTime);
		//gameRenderer.debugUpdate(game.currentMap());
		
		final long endTime = System.nanoTime();
		updateTime = (endTime - startTime) / 1000000.0f;
	}
	
	@Override
	public void onRender() {
		final long startTime = System.nanoTime();
		app.batch().begin();
		
		gameRenderer.render(game);
		gameUi.render();
		
		app.batch().end();
		final long endTime = System.nanoTime();
		
		game.console().updateAndRender(updateTime, (endTime - startTime) / 1000000.0f);
	}
	
	@Override
	public void dispose() {
		gameUi.dispose();
		game.dispose();
	}
}
