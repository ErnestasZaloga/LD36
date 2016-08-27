package com.company.minery.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.company.minery.Constants;
import com.company.minery.utils.AssetResolution;
import com.company.minery.utils.spine.SkeletonData;
import com.company.minery.utils.spine.SkeletonJson;

public class GameAssets implements Disposable {
	
	private AssetManager assetManager;
	private boolean loaded;
	
	private AssetResolution resolution; /**/ public final AssetResolution resolution() { return resolution; }
	private TextureAtlas tilesAtlas; /**/ public final TextureAtlas tilesAtlas() { return tilesAtlas; }
	private TextureAtlas testSkelAtlas; /**/ public final TextureAtlas testSkelAtlas() { return testSkelAtlas; }
	private TextureAtlas miscAtlas; /**/ public final TextureAtlas miscAtlas() { return miscAtlas; }
	private TextureAtlas decalsAtlas; /**/ public final TextureAtlas decalsAtlas() { return decalsAtlas; }
	private TextureAtlas blockAtlas; /**/ public final TextureAtlas blockAtlas() { return blockAtlas; }
	
	private SkeletonData testSkelData; /**/ public final SkeletonData testSkelData() { return testSkelData; }
	
	private TextureRegion block; /**/ public final TextureRegion block() { return block; }
	private TextureRegion ropePatternTest; /**/ public final TextureRegion ropePatternTest() { return ropePatternTest; }
	private TextureRegion ropePatternEndTest; /**/ public final TextureRegion ropePatternEndTest() { return ropePatternEndTest; }
	
	public final Element testMapXml;
	
	public GameAssets() {
		final XmlReader xmlParser = new XmlReader();
		testMapXml = xmlParser.parse(Gdx.files.internal("assets/maps/Map2.tmx").readString());
	}
	
	/**
	 * @return true if resolution have changed and assets were reloaded
	 * */
	public boolean loadSync(final AssetResolution resolution) {
		if(resolution == null) {
			throw new IllegalArgumentException("resolution cannot be null");
		}
		
		if(resolution == this.resolution) {
			// If assets of the same resolution is already loaded do nothing. 
			// this.resolution will be null if the assets are not loaded.
			// But the skeletons needs to be reloaded.
			loadSkeletons();
			return false;
		}
		else if(loaded) {
			dispose();
		}
		
		System.out.println("switching resolution");
		
		this.resolution = resolution;
		
		assetManager = new AssetManager();
		
		final String texturesFolder = "assets/textures/" + resolution.name;
		
		assetManager.load(texturesFolder + "/Tiles/Tiles.pack", TextureAtlas.class);
		assetManager.load(texturesFolder + "/Character/Character.pack", TextureAtlas.class);
		assetManager.load(texturesFolder + "/Decals/Decals.pack", TextureAtlas.class);
		assetManager.load(texturesFolder + "/Misc/Misc.pack", TextureAtlas.class);
		assetManager.load(texturesFolder + "/Block/Block.pack", TextureAtlas.class);
		
		assetManager.finishLoading();
		finishLoading();
		
		return true;
	}
	
	@Override
	public void dispose() {
		if(!loaded) {
			return;
		}
		
		assetManager.dispose();
		
		loaded = false;
		resolution = null;
		tilesAtlas = null;
		assetManager = null;
		testSkelAtlas = null;
		testSkelData = null;
		miscAtlas = null;
		decalsAtlas = null;
		ropePatternTest = null;
		ropePatternEndTest = null;
		blockAtlas = null;
		block = null;
	}
	
	/**
	 * Fetch the assets from the asset manager. 
	 * This has to be a separate method because assets can also be loaded in async way.
	 * */
	private void finishLoading() {
		final String texturesFolder = "assets/textures/" + resolution.name;
		
		tilesAtlas = assetManager.get(texturesFolder + "/Tiles/Tiles.pack", TextureAtlas.class);
		testSkelAtlas = assetManager.get(texturesFolder + "/Character/Character.pack", TextureAtlas.class);
		decalsAtlas = assetManager.get(texturesFolder + "/Decals/Decals.pack", TextureAtlas.class);
		miscAtlas = assetManager.get(texturesFolder + "/Misc/Misc.pack", TextureAtlas.class);
		blockAtlas = assetManager.get(texturesFolder + "/Block/Block.pack", TextureAtlas.class);
		
		ropePatternTest = miscAtlas.findRegion("RopePatternTest");
		ropePatternEndTest = miscAtlas.findRegion("RopePatternEnd");
		block = blockAtlas.findRegion("Block");
		
		loadSkeletons();
		loaded = true;
	}
	
	private void loadSkeletons() {
		final SkeletonJson json = new SkeletonJson(testSkelAtlas);
		json.setScale(Constants.EDITOR_RESOLUTION.calcScale() * 1.25f);
		testSkelData = json.readSkeletonData(Gdx.files.internal("assets/testSkel/Char1.json"));
	}
	
}
