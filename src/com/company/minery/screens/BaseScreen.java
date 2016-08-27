package com.company.minery.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Disposable;
import com.company.minery.App;

abstract public class BaseScreen implements Disposable {

	protected final App app;
	public final Color backgroundColor = new Color(1f, 1f, 1f, 1f);
	
	public BaseScreen(final App app) {
		this.app = app;
	}
	
	public void onShown() {
		// Intentionally left blank. Override for functionality.
	}
	
	public void onHidden() {
		// Intentionally left blank. Override for functionality.
	}

	public void onResize(final int newWidth, 
						 final int newHeight) {
		
		// Intentionally left blank. Override for functionality.
	}
	
	public void onPause() {
		// Intentionally left blank. Override for functionality.
	}
	
	public void onResume() {
		// Intentionally left blank. Override for functionality.
	}
	
	public void onUpdate(final float deltaTime) {
		// Intentionally left blank. Override for functionality.
	}
	
	abstract public void onRender();
	
	@Override
	public void dispose() {
		// Intentionally left blank. Override for functionality.
	}
}
