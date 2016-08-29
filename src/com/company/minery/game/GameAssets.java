package com.company.minery.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.company.minery.Constants;
import com.company.minery.utils.AssetResolution;

public class GameAssets implements Disposable {
	
	public class TextureRegionExt extends TextureRegion {
		
		private float width;
		private float height;
		
		public TextureRegionExt(final TextureRegion region) {
			this(region, 1f);
		}

		public TextureRegionExt(final TextureRegion region,
								final float scale) {
			
			super(region);
			
			if(region != null) {
				setSize(region.getRegionWidth() * scale, region.getRegionHeight() * scale);
			}
		}
		
		public TextureRegionExt(final TextureRegionExt region) {
			this(region, 1f);
		} 
		
		public TextureRegionExt(final TextureRegionExt region,
								final float scale) {
			
			super(region);
			
			if(region != null) {
				setSize(region.getWidth() * scale, region.getHeight() * scale);
			}
		}

		public void setSize(final float width,
							final float height) {
			
			this.width = width;
			this.height = height;
		}
		
		public void scale(final float scale) {
			this.width *= scale;
			this.height *= scale;
		}
		
		public void rescale(final float scale) {
			this.width = this.getRegionWidth() * scale;
			this.height = this.getRegionHeight() * scale;
		}
		
		public float getWidth() {
			return width;
		}
		
		public float getHeight() {
			return height;
		}
		
	}
	
	public AssetResolution resolution;
	
	public final TextureAtlas atlas;
	public final Element testMapXml;
	
	public final TextureRegionExt characterBody;
	public final TextureRegionExt characterFist;
	public final TextureRegionExt characterFoot;
	public final TextureRegionExt characterHead;
	public final TextureRegionExt spear;
	public final TextureRegionExt arrow;
	public final TextureRegionExt defeatLabel;
	public final TextureRegionExt victoryLabel;
	public final TextureRegionExt fightLabel;
	public final TextureRegionExt logo;
	public final TextureRegionExt start;
	public final TextureRegionExt matchmaking;
	public final TextureRegionExt credits;

	public final Music gameMusic;
	
	public final Sound fightSound;
	public final Sound jumpSound;
	public final Sound landSound;
	public final Sound loseSound;
	public final Sound stuckSound;
	public final Sound throwSound;
	public final Sound winSound;
	
	public GameAssets() {
		final XmlReader xmlParser = new XmlReader();
		testMapXml = xmlParser.parse(Gdx.files.internal("assets/maps/RuinedCastle.tmx").readString());
		
		atlas = new TextureAtlas("assets/textures/original/All/All.atlas");
		characterBody = new TextureRegionExt(atlas.findRegion("body"), 1);
		characterFist = new TextureRegionExt(atlas.findRegion("fist"), 1);
		characterFoot = new TextureRegionExt(atlas.findRegion("foot"), 1);
		characterHead = new TextureRegionExt(atlas.findRegion("head"), 1);
		spear = new TextureRegionExt(atlas.findRegion("spear"), 1);
		arrow = new TextureRegionExt(atlas.findRegion("arrow"), 1);
		defeatLabel = new TextureRegionExt(atlas.findRegion("defeat"), 1);
		victoryLabel = new TextureRegionExt(atlas.findRegion("victory"), 1);
		fightLabel = new TextureRegionExt(atlas.findRegion("fight"), 1);
		logo = new TextureRegionExt(atlas.findRegion("logo"), 1.5f);
		start = new TextureRegionExt(atlas.findRegion("start"), 2.5f);
		matchmaking = new TextureRegionExt(atlas.findRegion("enter_matchmaking"));
		credits = new TextureRegionExt(atlas.findRegion("credits"));
		
		gameMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/audio/game.mp3"));
		gameMusic.setLooping(true);
		gameMusic.setVolume(0.5f);
		
		fightSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/fight.mp3"));
		jumpSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/jump.mp3"));
		landSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/land.mp3"));
		loseSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/lose.mp3"));
		stuckSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/stuck.mp3"));
		throwSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/throw.mp3"));
		winSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/win.mp3"));
		
	}
	
	public final void rescale(final AssetResolution resolution) {
		this.resolution = resolution;
		
		characterBody.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
		characterFist.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
		characterFoot.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
		characterHead.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
		spear.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
		arrow.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
		defeatLabel.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
		victoryLabel.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
		fightLabel.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
		logo.rescale(Constants.PIXELART_SCALE * resolution.calcScale() * 1.5f);
		start.rescale(Constants.PIXELART_SCALE * resolution.calcScale() * 2.5f);
		matchmaking.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
		credits.rescale(Constants.PIXELART_SCALE * resolution.calcScale());
	}
	
	@Override
	public void dispose() {
		atlas.dispose();
	}
	
}
