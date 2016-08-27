package com.company.minery.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.company.minery.Constants;
import com.company.minery.game.console.Console;
import com.company.minery.game.map.Generator;
import com.company.minery.game.map.Map;
import com.company.minery.game.map.Tunnel;
import com.company.minery.game.multiplayer.GameClient;
import com.company.minery.game.multiplayer.GameLocalClient;
import com.company.minery.game.multiplayer.GameRemoteClient;
import com.company.minery.game.pawn.Player;
import com.company.minery.game.pawn.input.InputTranslator;
import com.company.minery.gameui.GameUi;
import com.company.minery.utils.AssetResolution;

public final class Game implements Disposable {
	
	private final GameLocalClient localClient;
	private final GameRemoteClient remoteClient;
	
	public final GameAssets assets;
	
	private Player localPlayer; /**/ public Player localPlayer() { return localPlayer; }
	public final Array<Player> players = new Array<Player>();
	
	private final GameListener gameListener;
	private final GameUi ui;
	private final InputTranslator inputTranslator; /**/ public InputTranslator inputTranslator() { return inputTranslator; }
	private final Runnable remoteDisconnectCallback = new Runnable() {
		@Override
		public void run() {
			switchToLocal();
		}
	};
	
	private GameClient client;
	private float lastSizeScale;
	private boolean playing;
	
	private float cameraFollowSpeed = 0.2f; // TODO: make adjustable
	private Map currentMap; /**/ public Map currentMap() { return currentMap; }
	
	// XXX: DEBUG ONLY
	private boolean panningEnabled; /**/ public boolean panningEnabled() { return panningEnabled; }
	private Console console; /**/ public Console console() { return console; }
	private float lastX;
	private float lastY;
	
	public Game(final GameUi ui,
				final GameListener gameListener) {
		
		if(gameListener == null) {
			throw new IllegalArgumentException("gameListener cannot be null");
		}

		this.gameListener = gameListener;
		this.assets = new GameAssets();
		this.ui = ui;

		localClient = new GameLocalClient(this);
		remoteClient = new GameRemoteClient(this, remoteDisconnectCallback);
		
		inputTranslator = new InputTranslator(this);
		inputTranslator.setMovementKeys(Keys.A, Keys.D, Keys.W);
		inputTranslator.setMouseControls(Buttons.LEFT, Buttons.RIGHT);
		
		//TODO: remove on release
		Gdx.input.setInputProcessor(new InputProcessor() {
			@Override
			public boolean keyDown(int keycode) {return false;}
			@Override
			public boolean keyUp(int keycode) {return false;}
			@Override
			public boolean keyTyped(char character) {return false;}
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer,int button) {return false;}
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer,int button) {return false;}
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {return false;}
			@Override
			public boolean mouseMoved(int screenX, int screenY) {return false;}

			@Override
			public boolean scrolled(int amount) {
				if(amount == 1) {
					if(AssetResolution.ZOOM >= 1) {
						AssetResolution.ZOOM = Math.round(AssetResolution.ZOOM);
						AssetResolution.ZOOM++;
					} else {
						AssetResolution.ZOOM *= 2;
					}
				} else {
					if(AssetResolution.ZOOM > 1) {
						AssetResolution.ZOOM = Math.round(AssetResolution.ZOOM);
						AssetResolution.ZOOM--;
					} else {
						AssetResolution.ZOOM /= 2f;
					}
				}
				
				setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), assets.resolution());
				return false;
			}
		});
	}
	
	public void setLocalPlayer(final Player localPlayer) {
		this.localPlayer = localPlayer;
	}
	
	public void begin() {
		localPlayer = new Player(true);
		localPlayer.runSpeedPerc = 6f;
		localPlayer.jumpHeightPerc = 5f;
		
		inputTranslator.setListener(localPlayer);
		
		players.clear();
		players.add(localPlayer);
		
		client = localClient;
		localClient.begin(Constants.DEFAULT_TCP_PORT, Constants.DEFAULT_UDP_PORT);
		
		currentMap = Generator.generateTestMap(assets);
		
		lastSizeScale = Constants.EDITOR_RESOLUTION.calcScale();
		
		currentMap.setScale(lastSizeScale);
		
		final Tunnel startTunnel = currentMap.findTunnelByName("start");
		
		localPlayer.x = startTunnel.x + startTunnel.width / 2f - localPlayer.width() / 2f;
		localPlayer.y = startTunnel.y;
		
		currentMap.pawns.add(localPlayer);
		
		console = new Console(this);
		
		playing = true;
	}
	
	public void setSize(final float width, 
						final float height,
						final AssetResolution assetResolution) {
		
		final boolean reload = assets.loadSync(assetResolution);
		// TODO: apply appearances
		for(int i = 0; i < players.size; i += 1) {
			players.get(i).applySkeletonData(assets.testSkelData());
		}
		
		if(playing) {
			final float sizeScale = Constants.EDITOR_RESOLUTION.calcScale();
			final float sizeRescale = sizeScale / lastSizeScale;
			lastSizeScale = sizeScale;

			currentMap.setScale(sizeRescale);
			
			if(reload) {
				currentMap.assetLoader.load(currentMap, assets);
			}
			
			console.setSize(width, height);
		}
		
		System.out.println("current resolution: " + assets.resolution().name);
	}
	

	public void update(final float delta) {
		/// TODO: remove on release
		if(Gdx.input.isKeyJustPressed(Keys.C)) {
			console.setActive(!console.active());
		}
		
		if(playing) {
			if(!console.active()) {
				inputTranslator.update();
			}
			
			client.update(delta);
			
			// Adjust the view to player
			if(!panningEnabled) {
				final Map currentMap = this.currentMap;
				final float viewX = currentMap.viewX();
				final float viewY = currentMap.viewY();

				final float targetViewX = localPlayer.x + localPlayer.width() / 2f - Gdx.graphics.getWidth() / 2f;
				final float targetViewY = localPlayer.y + localPlayer.height() / 2f - Gdx.graphics.getHeight() / 2f;
				
				final float percent = delta / cameraFollowSpeed;
				
				currentMap.setViewPosition(
						viewX + (targetViewX - viewX) * percent, 
						viewY + (targetViewY - viewY) * percent);
			}
			else {
				handlePan();
			}
			
			final float screenWidth = Gdx.graphics.getWidth();
			final float screenHeight = Gdx.graphics.getHeight();
			final float mapWidth = currentMap.tileWidth() * currentMap.mainLayer.tiles.width;
			final float mapHeight = currentMap.tileHeight() * currentMap.mainLayer.tiles.height;

			// Limit view position
			{
				float viewX = currentMap.viewX();
				float viewY = currentMap.viewY();
				
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
		}
	}
	
	// XXX: DEBUG ONLY
	public void togglePanning() {
		panningEnabled = !panningEnabled;
	}
	
	// XXX: DEBUG ONLY
	private void handlePan() {
		if(Gdx.input.isTouched()) {
			final float x = Gdx.input.getX();
			final float y = Gdx.graphics.getHeight() - Gdx.input.getY();
			
			if(!Gdx.input.justTouched()) {
				final float deltaX = x - lastX;
				final float deltaY = y - lastY;
			
				currentMap.setViewPosition(currentMap.viewX() - deltaX, currentMap.viewY() - deltaY);
			}
			
			lastX = x;
			lastY = y;
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
