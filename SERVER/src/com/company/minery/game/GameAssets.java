package com.company.minery.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.company.minery.Constants;

public class GameAssets implements Disposable {
	
	public class TextureRegionExt {
		
		private float originalWidth;
		private float originalHeight;
		
		private float width;
		private float height;
		
		public TextureRegionExt(final float width, final float height) {
			this.originalWidth = width;
			this.originalHeight = height;
			this.width = originalWidth;
			this.height = originalHeight;
		}
		
		public float getWidth() {
			return width;
		}
		
		public float getHeight() {
			return height;
		}
		
	}
	
	public final Element testMapXml;
	
	public final TextureRegionExt characterBody;
	public final TextureRegionExt characterFist;
	public final TextureRegionExt characterFoot;
	public final TextureRegionExt characterHead;
	public final TextureRegionExt spear;
	
	public GameAssets() {
		final XmlReader xmlParser = new XmlReader();
		testMapXml = xmlParser.parse(Gdx.files.internal("assets/maps/RuinedCastle.tmx").readString());
		
		// BODY 20x23
		// FIST 6x5
		// FOOT 10x4
		// HEAD 17x13
		// SPEAR 48x4
		
		characterBody = new TextureRegionExt(Constants.PIXELART_SCALE * 20, Constants.PIXELART_SCALE * 23);
		characterFist = new TextureRegionExt(Constants.PIXELART_SCALE * 6, Constants.PIXELART_SCALE * 5);
		characterFoot = new TextureRegionExt(Constants.PIXELART_SCALE * 10, Constants.PIXELART_SCALE * 4);
		characterHead = new TextureRegionExt(Constants.PIXELART_SCALE * 17, Constants.PIXELART_SCALE * 13);
		spear = new TextureRegionExt(Constants.PIXELART_SCALE * 48, Constants.PIXELART_SCALE * 4);
	}
	
	@Override
	public void dispose() {
	}
	
}
