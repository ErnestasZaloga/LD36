package ludumdare.ld36;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;

public final class App implements ApplicationListener {

	public static final void main(final String[] arguments) {
		final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.width = 320;
		config.height = 480;
		config.title = "Hello";

        new LwjglApplication(new App(), config);
	}

	@Override
	public void create() {
		System.out.println("Hello world!");
	}

	@Override
	public void resize(final int width, final int height) {
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}
	
}