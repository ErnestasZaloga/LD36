package com.company.minery.game;

import com.badlogic.gdx.utils.Array;
import com.company.minery.game.map.Map;
import com.company.minery.game.player.PhysicalObject;
import com.company.minery.game.player.Player;
import com.company.minery.game.player.Spear;

public final class Game {
	
	public final Map map;
	public final GameAssets assets;
	
	public final Array<Player> players = new Array<Player>();
	public final Array<Spear> spears = new Array<Spear>();
	public final Array<PhysicalObject> physicalObjects = new Array<PhysicalObject>();
	
	public Game(final Map map,
				final GameAssets assets) {
		
		this.map = map;
		this.assets = assets;
	}
	
}
