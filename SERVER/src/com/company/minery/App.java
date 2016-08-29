package com.company.minery;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.company.minery.game.multiplayer.GameServer;

public class App implements ApplicationListener {
	
	private GameServer server;
	
	@Override
	public void create() {
		server = new GameServer();
		server.begin(Constants.DEFAULT_TCP_PORT, Constants.DEFAULT_UDP_PORT);
	}

	@Override
	public void resize(final int width, 
					   final int height) {}
	
	@Override
	public void render() {
		final float deltaTime = Gdx.graphics.getDeltaTime();
		server.update(deltaTime > 1f / 30f ? 1f / 30f : deltaTime);
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}
	
	@Override
	public void dispose() {
		server.end();
	}
	
}
