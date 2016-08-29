package com.company.minery.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.company.minery.App;
import com.company.minery.game.Game;
import com.company.minery.game.GameRender;

public class GameScreen extends BaseScreen {
	
	public final Game game;
	private final GameRender gameRenderer;
	
	public GameScreen(final App app) {
		super(app);
		
		backgroundColor.set(Color.valueOf("000000"));
		
		game = new Game(app);
		gameRenderer = new GameRender(app.batch());
	}
	
	public void beginGame() {
		onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		game.begin();
	}
	
	@Override
	public void onResize(final int newWidth, 
						 final int newHeight) {
		
		game.setSize(newWidth, newHeight, app.assetResolution());
	}
	
	@Override
	public void onUpdate(final float deltaTime) {
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			game.end();
			game.exit();
			return;
		}
		
		game.update(deltaTime);
	}
	
	@Override
	public void onRender() {
		app.batch().begin();
		
		gameRenderer.render(game);
		app.batch().end();
	}
	
	@Override
	public void dispose() {
		game.dispose();
	}
}
