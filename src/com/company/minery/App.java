package com.company.minery;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.company.minery.screens.BaseScreen;
import com.company.minery.screens.GameScreen;
import com.company.minery.utils.AssetResolution;
import com.company.minery.utils.JumpUtil;

public class App implements ApplicationListener {
	
	private BaseScreen activeScreen; /**/ public final BaseScreen activeScreen() { return activeScreen; }
	private PolygonSpriteBatch batch; /**/ public final PolygonSpriteBatch batch() { return batch; }
	private GameScreen gameScreen; /**/ public final GameScreen gameScreen() { return gameScreen; }
	private AssetResolution assetResolution; /**/ public final AssetResolution assetResolution() { return assetResolution; }
	private OrthographicCamera camera; /**/ public final OrthographicCamera camera() { return camera; }
	
	@Override
	public void create() {
		//Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode());
		
		final int screenWidth = Gdx.graphics.getWidth();
		final int screenHeight = Gdx.graphics.getHeight();
		
		System.out.println("Screen width: " + screenWidth + " screen height: " + screenHeight);
		
		camera = new OrthographicCamera();
		batch = new PolygonSpriteBatch(1000);
		assetResolution = pickAssetResolution(screenWidth, screenHeight);
		
		gameScreen = new GameScreen(this);
		setScreen(gameScreen);
		gameScreen.beginGame();
	}

	@Override
	public void resize(final int width, 
					   final int height) {

		assetResolution = pickAssetResolution(width, height);
		System.out.println("res scale: " + Constants.EDITOR_RESOLUTION.calcScale());
		
		System.out.println("width: " + width + " height: " + height);
		Gdx.gl.glViewport(0, 0, width, height);
		camera.setToOrtho(false, width, height);
		camera.position.set(width / 2f, height / 2f, 0f);
		
		if(activeScreen != null) {
			activeScreen.onResize(width, height);
		}
	}
	
	@Override
	public void render() {
		if(activeScreen != null) {
			final Color backgroundColor = activeScreen.backgroundColor;
			
			Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			
			final float deltaTime = Gdx.graphics.getDeltaTime();
			
			camera.update();
			batch.setProjectionMatrix(camera.combined);
			
			activeScreen.onUpdate(deltaTime > 1f / 30f ? 1f / 30f : deltaTime);
			activeScreen.onRender();
		}
		else {
			Gdx.gl.glClearColor(1, 1, 1, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
	}

	@Override
	public void pause() {
		if(activeScreen != null) {
			activeScreen.onPause();
		}
	}

	@Override
	public void resume() {
		if(activeScreen != null) {
			activeScreen.onResume();
		}
	}
	
	@Override
	public void dispose() {
		gameScreen.dispose();
		batch.dispose();
	}
	
	private AssetResolution pickAssetResolution(final int width, 
									 			final int height) {
	
		final AssetResolution[] possibleResolutions = Constants.RESOLUTION_LIST;
		
		final int smallerScreenDimension = width > height ? height : width;
		
		AssetResolution bestResolution = possibleResolutions[0];
		float bestResolutionScale = Math.abs(bestResolution.calcRequiredResolutionScale(smallerScreenDimension) - 1);
		
		for(final AssetResolution resolution : possibleResolutions) {
			final float resolutionScale = Math.abs(resolution.calcRequiredResolutionScale(smallerScreenDimension) - 1);
			
			if(resolutionScale < bestResolutionScale) {
				bestResolutionScale = resolutionScale;
				bestResolution = resolution;
			}
		}
		
		return bestResolution;
	}
	
	/**
	 * Changes the active screen to the passed screen object.
	 * @param screen - the new active screen. Can be null.
	 * */
	public void setScreen(final BaseScreen screen) {
		if(activeScreen != null) {
			activeScreen.onHidden();
		}
		
		activeScreen = screen;
		
		if(activeScreen != null) {
			activeScreen.onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			activeScreen.onShown();
		}
	}
	
}
