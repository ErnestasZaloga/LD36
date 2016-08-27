package com.company.minery.game.takable;

import com.badlogic.gdx.utils.Pool.Poolable;

public abstract class Takable implements Poolable{
	
	private int id; /**/ public final int id() { return id; }
	
	private float x; /**/ public final float x() { return x; }
	private float y; /**/ public final float y() { return y; }
	private float width; /**/ public final float width() { return width; }
	private float height; /**/ public final float height() { return height; }
	
	public Takable() {
		
	}
	
	public final void setId(final int id) {
		this.id = id;
	}
	
	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	public final void setWidth(final float width) {
		this.width = width;
	}

	public final  void setHeight(final float height) {
		this.height = height;
	}
	
	@Override
	public void reset() {
		width = 0;
		height = 0;
		id = -1;
	}
}
