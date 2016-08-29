package com.company.minery.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.company.minery.App;
import com.company.minery.Constants;
import com.company.minery.game.GameAssets.TextureRegionExt;
import com.company.minery.game.console.Console;
import com.company.minery.game.map.Generator;
import com.company.minery.game.map.Map;
import com.company.minery.game.map.MapLocation;
import com.company.minery.game.multiplayer.GameClient;
import com.company.minery.game.multiplayer.GameEndpoint;
import com.company.minery.game.multiplayer.GameServer;
import com.company.minery.game.player.InputTranslator;
import com.company.minery.game.player.Player;
import com.company.minery.game.player.Spear;
import com.company.minery.gameui.GameUi;
import com.company.minery.utils.AssetResolution;

public final class Game implements Disposable {
	
	private final GameServer localClient;
	private final GameClient remoteClient;
	
	public final GameAssets assets;
	
	private Player localPlayer; /**/ public Player localPlayer() { return localPlayer; }
	public final Array<Player> players = new Array<Player>();
	public final Array<Spear> spears = new Array<Spear>();
	
	private final GameListener gameListener;
	private final GameUi ui;
	public final InputTranslator inputTranslator;
	private final Runnable remoteDisconnectCallback = new Runnable() {
		@Override
		public void run() {
			switchToLocal();
		}
	};
	
	private GameEndpoint client;
	private float lastSizeScale;
	private boolean playing;
	
	public float messageTimer;
	public TextureRegionExt message;
	
	private Map currentMap; /**/ public Map currentMap() { return currentMap; }
	
	private Console console; /**/ public Console console() { return console; }
	
	public final App app;
	public Game(final App app,
				final GameUi ui,
				final GameListener gameListener) {
		
		if(gameListener == null) {
			throw new IllegalArgumentException("gameListener cannot be null");
		}
		
		this.app = app;

		this.gameListener = gameListener;
		this.assets = new GameAssets();
		this.ui = ui;

		localClient = new GameServer(this);
		remoteClient = new GameClient(this, remoteDisconnectCallback);
		
		inputTranslator = new InputTranslator(this);
		inputTranslator.setMovementKeys(Keys.A, Keys.D, Keys.W);
	}
	
	public void setLocalPlayer(final Player localPlayer) {
		this.localPlayer = localPlayer;
	}
	
	public void begin() {
		localPlayer = new Player(true);
		
		inputTranslator.setListener(localPlayer);
		
		players.clear();
		spears.clear();
		
		players.add(localPlayer);
		
		client = localClient;
		localClient.begin(Constants.DEFAULT_TCP_PORT, Constants.DEFAULT_UDP_PORT);
		
		currentMap = Generator.generateTestMap(assets);
		
		lastSizeScale = assets.resolution.calcScale();
		
		currentMap.setScale(lastSizeScale * Constants.PIXELART_SCALE);
		
		final MapLocation startLocation = currentMap.findLocationByName("p1_start");
		
		localPlayer.x = startLocation.x + startLocation.width / 2f - localPlayer.width / 2f;
		localPlayer.y = startLocation.y;
		
		currentMap.physicalObjects.add(localPlayer);
		
		console = new Console(this);
		
		playing = true;
	}
	
	public void end() {
		playing = false;
		client.end();
	}
	
	public void setSize(final float width, 
						final float height,
						final AssetResolution assetResolution) {
		
		assets.rescale(assetResolution);
		
		// TODO: apply appearances
		for(int i = 0; i < players.size; i += 1) {
			players.get(i).applyAppearance(assets);
		}
		
		if(playing) {
			final float sizeScale = assets.resolution.calcScale();
			final float sizeRescale = sizeScale / lastSizeScale;
			lastSizeScale = sizeScale;

			currentMap.setScale(sizeRescale);
			currentMap.assetLoader.load(currentMap, assets);
			
			console.setSize(width, height);
		}
	}
	
	private void limitView() {
		final float screenWidth = Gdx.graphics.getWidth();
		final float screenHeight = Gdx.graphics.getHeight();
		final float mapWidth = currentMap.tileWidth * currentMap.mainLayer.tiles.width;
		final float mapHeight = currentMap.tileHeight * currentMap.mainLayer.tiles.height;
		
		float viewX = currentMap.viewX;
		float viewY = currentMap.viewY;
		
		if(viewX < 0f) {
			viewX = 0f;
		}
		if(viewX + screenWidth > mapWidth) {
			viewX = mapWidth - screenWidth;
		}
		
		if(viewY < 0f) {
			viewY = 0f;
		}
		if(viewY + screenHeight > mapHeight) {
			viewY = mapHeight - screenHeight;
		}
		
		currentMap.setViewPosition(viewX, viewY);
	}
	
	private void updateView(final float delta) {
		// Adjust the view to player
		{
			final Map currentMap = this.currentMap;
			final float viewX = currentMap.viewX;
			final float viewY = currentMap.viewY;
			
			final float targetViewX = localPlayer.x + localPlayer.width / 2f - Gdx.graphics.getWidth() / 2f;
			final float targetViewY = localPlayer.y + localPlayer.height / 2f - Gdx.graphics.getHeight() / 2f;
			
			final float percent = delta / Constants.CAMERA_FOLLOW_SPEED;
			
			currentMap.setViewPosition(
					viewX + (targetViewX - viewX) * percent, 
					viewY + (targetViewY - viewY) * percent);
		}
		
		limitView();
	}

	public void update(final float delta) {
		if(Gdx.input.isKeyJustPressed(Keys.MINUS)) {
			console.setActive(!console.active());
		}

		if(message != null) {
			messageTimer += delta;
			
			if(messageTimer >= Constants.MESSAGE_TIME) {
				if(Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Keys.ANY_KEY)) {
					message = null;
					
					if(message != assets.fightLabel) {
						playing = false;
						app.setScreen(app.menuScreen);
					}
				}
			}
		}
		
		if(playing) {
			if(!console.active()) {
				inputTranslator.update();
			}
			
			client.update(delta);
			updateView(delta);
		}
	}
	
	public void switchToRemote(final String ipAddress) {
		if(client != remoteClient) {
			client.end();
			client = remoteClient;
			remoteClient.begin(ipAddress, Constants.DEFAULT_TCP_PORT, Constants.DEFAULT_UDP_PORT);
		}
	}
	
	public void switchToLocal() {
		if(client != localClient) {
			client.end();
			client = localClient;
			localClient.begin(Constants.DEFAULT_TCP_PORT, Constants.DEFAULT_UDP_PORT);
		}
	}
	
	@Override
	public void dispose() {
		assets.dispose();
	}
	
}
