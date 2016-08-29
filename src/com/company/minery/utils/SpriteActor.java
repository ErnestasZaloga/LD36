package com.company.minery.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.company.minery.game.GameAssets.TextureRegionExt;

public class SpriteActor extends Actor {

    private final Sprite sprite;
	private TextureRegionExt region;
	private float sizeScale = 1f;

    private float lastX = Float.MIN_VALUE;
    private float lastY = Float.MIN_VALUE;

    private float lastR = 1f;
    private float lastG = 1f;
    private float lastB = 1f;
    private float lastA = 1f;

	public SpriteActor() {
		this.sprite = new Sprite();
		sprite.setOrigin(0f, 0f);
	}
	
	public SpriteActor(final TextureRegionExt region) {
		this.region = region;
		this.sprite = new Sprite(region);
		
		final float prefWidth = region.getWidth();
		final float prefHeight = region.getHeight();

		sprite.setOrigin(0f, 0f);

		setSize(prefWidth, prefHeight);
	}

	public void setSizeScale(final float sizeScale) {
		this.sizeScale = sizeScale;
		applySizeScale();
	}
	
	public float getSizeScale() {
		return sizeScale;
	}
	
	public Sprite getSprite() {
		return sprite;
	}
	
	public void setRegion(final TextureRegionExt region) {
		if(region == null) {
			sprite.setTexture(null);
		} 
		else {
			sprite.setRegion(region);
		}

        lastX = Float.MIN_VALUE;
        lastY = Float.MIN_VALUE;
		
		this.region = region;
		
		if(region != null) {
			applySizeScale();
		} 
		else {
			setSize(0f, 0f);
			sprite.setSize(0f, 0f);
		}
	}
	
	public TextureRegionExt getRegion() {
		return region;
	}

	private void applySizeScale() {
		if(region == null) {
			setSize(0f, 0f);
			return;
		}
        setSize(checkedMetricsValue(region.getWidth() * sizeScale), checkedMetricsValue(region.getHeight() * sizeScale));
	}

    private float checkedMetricsValue(final float value) {
        if(region != null) {
            return (int) value;
        }
        return value;
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        sprite.setSize(checkedMetricsValue(getWidth()), checkedMetricsValue(getHeight()));
    }
	
	@Override
	public void setOriginX(final float originX) {
		super.setOriginX(originX);
		sprite.setOrigin(checkedMetricsValue(originX), checkedMetricsValue(getOriginY()));
	}
	
	@Override
	public void setOriginY(final float originY) {
		super.setOriginY(originY);
		sprite.setOrigin(checkedMetricsValue(getOriginX()), checkedMetricsValue(originY));
	}
	
	@Override
	public void setOrigin(final float originX, 
						  final float originY) {
		
		super.setOrigin(originX, originY);
		sprite.setOrigin(checkedMetricsValue(originX), checkedMetricsValue(originY));
    }

    @Override
    public void setOrigin(final int alignment) {
        super.setOrigin(alignment);
        sprite.setOrigin(checkedMetricsValue(getOriginX()), checkedMetricsValue(getOriginY()));
    }

	@Override
	public void setScaleX(final float scaleX) {
		super.setScaleX(scaleX);
		sprite.setScale(scaleX, getScaleY());
    }
	
	@Override
	public void setScaleY(final float scaleY) {
		super.setScaleY(scaleY);
		sprite.setScale(getScaleX(), scaleY);
	}
	
	@Override
	public void setScale(final float scaleX, 
						 final float scaleY) {
		
		super.setScale(scaleX, scaleY);
		sprite.setScale(scaleX, scaleY);
	}
	
	@Override
	public void setScale(final float scale) {
		super.setScale(scale);
		sprite.setScale(scale);
	}
	
	@Override
	public void scaleBy(final float scale) {
		super.scaleBy(scale);
		sprite.scale(scale);
	}
	
	@Override
	public void scaleBy(final float scaleX, 
					    final float scaleY) {
		
		super.scaleBy(scaleX, scaleY);
		sprite.setScale(getScaleX(), getScaleY());
	}
	
	@Override
	public void setRotation(final float rotation) {
		super.setRotation(rotation);
		sprite.setRotation(rotation);
	}
	
	@Override
	public void rotateBy(final float degrees) {
		super.rotateBy(degrees);
		sprite.rotate(degrees);
	}
	
	@Override
	public void draw(final Batch batch,
					 final float parentAlpha) {
		
		if(region != null) {
            final float x = getX();
            final float y = getY();
            final Color color = getColor();

            if(color.r != lastR || color.g != lastG || color.b != lastB || color.a != lastA) {
                lastR = color.r;
                lastG = color.g;
                lastB = color.b;
                lastA = color.a;

                sprite.setColor(color.r, color.g, color.b, color.a);
            }

            if(x != lastX || y != lastY) {
                lastX = x;
                lastY = y;

                sprite.setPosition(checkedMetricsValue(x), checkedMetricsValue(y));
            }

			sprite.draw(batch, parentAlpha);
		}
	}
	
}