package com.company.minery.game.map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.company.minery.game.GameAssets;

public final class MapAssetLoader {
	
	public void load(final Map map, 
					 final GameAssets assets) {
		
		final TextureAtlas[] atlases = new TextureAtlas[] {
				assets.atlas
		};

		final Layer[] layers = map.layers;
		final int n = layers.length;
		
		for(int i = 0; i < n; i += 1) {
			final Layer layer = layers[i];
			final Tile[] tileset = layer.tiles.tileset;
			
			for(int ii = 0, nn = tileset.length; ii < nn; ii += 1) {
				// Find region
				TextureRegion region = null;
				for(int iii = 0, nnn = atlases.length; iii < nnn; iii += 1) {
					final String regionLookupName = tileset[ii].regionLookupName;
					region = atlases[iii].findRegion(regionLookupName);
					
					if(region != null) {
						break;
					}
				}
				
				if(region == null) {
					throw new RuntimeException("Tile named " + tileset[ii].regionLookupName + " not found in given atlases");
				}
				
				tileset[ii].region = region;
			}
			
			final StaticDecoration[] decorations = layer.decorations;
			
			if(decorations == null) {
				continue;
			}
			
			for(int ii = 0, nn = decorations.length; ii < nn; ii++) {
				final StaticDecoration deco = decorations[ii];
				final Tile tile = tileset[deco.gid - 1];
				TextureRegion region = tile.region;
				deco.setRegion(region);
				deco.setWidth(tile.width);
				deco.setHeight(tile.height);
			}
		}
	}
	
}
