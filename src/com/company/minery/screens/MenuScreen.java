package com.company.minery.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.company.minery.App;
import com.company.minery.game.GameAssets;
import com.company.minery.utils.SpriteActor;

public class MenuScreen extends BaseScreen {

	private final Stage stage;
	private final GameAssets assets;
	private final SpriteActor logo = new SpriteActor();
	private final SpriteActor start = new SpriteActor();
	
	public MenuScreen(final App app, final GameAssets assets) {
		super(app);
		this.assets = assets;
		stage = new Stage(new ScreenViewport(), app.batch);
		
		logo.setRegion(assets.logo);
		start.setRegion(assets.start);
		
		stage.addActor(logo);
		stage.addActor(start);
		
		start.addAction(Actions.forever(Actions.sequence(Actions.visible(false), Actions.delay(0.5f), Actions.visible(true), Actions.delay(0.5f))));
	}
	
	@Override
	public void onResize(final int newWidth, 
						 final int newHeight) {
		
		app.gameScreen.onResize(newWidth, newHeight);
		stage.getViewport().update(newWidth, newHeight, true);
		
		logo.setRegion(assets.logo);
		start.setRegion(assets.start);
		
		logo.setPosition(newWidth / 2f - logo.getWidth() / 2f, newHeight - logo.getHeight() * 1.5f);
		start.setPosition(newWidth / 2f - start.getWidth() / 2f, logo.getY() / 2f - start.getHeight() / 2f);
	}
	
	@Override
	public void onUpdate(final float deltaTime) {
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
			return;
		}
		if(Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Keys.ANY_KEY)) {
			app.gameScreen.beginGame();
			app.setScreen(app.gameScreen);
		}
		
		stage.act(deltaTime);
	}
	
	@Override
	public void onRender() {
		stage.draw();
	}
	
	@Override
	public void dispose() {
	}

}
