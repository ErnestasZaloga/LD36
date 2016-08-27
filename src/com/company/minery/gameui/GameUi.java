package com.company.minery.gameui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

public class GameUi implements Disposable {

	private final GameUiListener listener;
	
	public GameUi(final GameUiListener listener,
				  final Batch batch) {
		
		this.listener = listener;
	}
	
	public void update(final float delta) {
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			listener.onUserRequestedExit();
		}
	}
	
	public void render() {
	}
	
	public void begin() {
		
	}
	
	public void setSize(final float width, 
						final float height) {
		
		
	}
	
	@Override
	public void dispose() {
		
	}
	
}
