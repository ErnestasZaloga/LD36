package com.company.minery.game.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.company.minery.Constants;
import com.company.minery.game.Game;
import com.company.minery.game.GameAssets;
import com.company.minery.game.GameUpdate;
import com.company.minery.game.map.Generator;
import com.company.minery.game.map.Map;
import com.company.minery.game.map.MapLocation;
import com.company.minery.game.multiplayer.messages.ClientAssignmentMessage;
import com.company.minery.game.multiplayer.messages.ImpulseMessage;
import com.company.minery.game.multiplayer.messages.ObjectMessage;
import com.company.minery.game.multiplayer.messages.PlayerMessage;
import com.company.minery.game.multiplayer.messages.SpearMessage;
import com.company.minery.game.multiplayer.messages.WorldStateMessage;
import com.company.minery.game.player.PhysicalObject;
import com.company.minery.game.player.Player;
import com.company.minery.game.player.Spear;
import com.company.minery.utils.kryonet.Connection;
import com.company.minery.utils.kryonet.Listener;
import com.company.minery.utils.kryonet.Server;

public final class GameServer {

	private static final class GameConnection {
		
		public final Game game;
		public final GameServerConnection player1;
		public final GameServerConnection player2;
		public long lastImpulseTime;
		
		public GameConnection(final Game game,
							  final GameServerConnection player1,
							  final GameServerConnection player2) {
			
			this.game = game;
			this.player1 = player1;
			this.player2 = player2;
			this.lastImpulseTime = System.currentTimeMillis();
		}
		
	}
	
	private static final class Message {
		
		public final GameServerConnection connection;
		public final Object object;
		
		public Message(final GameServerConnection connection,
					   final Object object) {
			
			this.connection = connection;
			this.object = object;
		}
		
	}
	
	private final GameAssets assets;
	private final Map map;
	
	private final GameUpdate worldUpdate = new GameUpdate();
	private final Server server;
	
	private final Array<Message> pendingMessages = new Array<Message>();
	private final Array<GameServerConnection> pendingConnections = new Array<GameServerConnection>();
	private final Array<GameServerConnection> pendingDisconnections = new Array<GameServerConnection>();
	
	private final Array<GameConnection> gameConnections = new Array<GameConnection>();
	
	public GameServer() {
		this.assets = new GameAssets();
		this.map = Generator.generateTestMap(assets);
		
		server = new Server() {
			
			@Override
			protected Connection newConnection() {
				return new GameServerConnection(new Player());
			}
			
		};
		
		server.addListener(new Listener() {
			
			@Override
			public void connected(final Connection connection) {
				System.out.println("Client connected");

				Gdx.app.postRunnable(new Runnable() {
					
					@Override
					public final void run() {
						pendingConnections.add((GameServerConnection) connection);
					}
					
				});
			}
			
			@Override
			public void disconnected(final Connection connection) {
				System.out.println("Client disconnected");
				
				Gdx.app.postRunnable(new Runnable() {
					
					@Override
					public final void run() {
						pendingDisconnections.add((GameServerConnection) connection);
					}
					
				});
			}
			
			@Override
			public void received(final Connection connection, 
								 final Object object) {

				Gdx.app.postRunnable(new Runnable() {
					
					@Override
					public final void run() {
						pendingMessages.add(new Message((GameServerConnection) connection, object));
					}
					
				});
			}
			
			@Override
			public void idle(final Connection connection) {}
			
		});
		
		Multiplayer.register(server);
	}
	
	public void begin(final int tcpPort, 
					  final int udpPort) {
		
		server.start();
		
		try {
			server.bind(tcpPort, udpPort);
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			Gdx.app.exit();
		}
	}
	
	public void end() {
		server.stop();
		
		pendingMessages.clear();
		pendingConnections.clear();
		pendingDisconnections.clear();
		gameConnections.clear();
	}
	
	private void processImpulses() {
		final Array<Message> pendingMessages = this.pendingMessages;
		final Array<GameConnection> gameConnections = this.gameConnections;
		
		for(int i = 0; i < pendingMessages.size; i += 1) {
			final Message message = pendingMessages.get(i);
			final GameServerConnection connection = message.connection;
			final Object object = message.object;
			
			if(object instanceof ImpulseMessage) {
				final ImpulseMessage impulse = (ImpulseMessage) object;
				final long time = impulse.messageTime;
				
				if(connection.impulseTimeThreshold() <= time) {
					connection.setImpulseTimeThreshold(time);
				
					final byte flag = impulse.movementFlag;
					if(flag == ImpulseMessage.FLAG_MOVE_IDLE) {
						connection.player.onIdle();
					}
					else if(flag == ImpulseMessage.FLAG_MOVE_LEFT) {
						connection.player.onLeftPressed();
					}
					else if(flag == ImpulseMessage.FLAG_MOVE_RIGHT) {
						connection.player.onRightPressed();
					}
					
					if(impulse.jumpFlag) {
						connection.player.onJumpPressed();
					}
					if(impulse.attackFlag) {
						final float scale = 1f / impulse.scale;
						connection.player.onAttackPressed(impulse.attackX * scale, impulse.attackY * scale);
					}
					
					for(int ii = 0; ii < gameConnections.size; ii += 1) {
						final GameConnection gameConnection = gameConnections.get(ii);
						if(connection.player.game == gameConnection.game) {
							gameConnection.lastImpulseTime = System.currentTimeMillis();
							break;
						}
					}
				}
			}
		}

		pendingMessages.clear();
	}
	
	private final void processDisconnections() {
		final Array<GameServerConnection> pendingDisconnections = this.pendingDisconnections;
		final Array<GameServerConnection> pendingConnections = this.pendingConnections;
		
		for(int i = 0; i < pendingDisconnections.size; i += 1) {
			if(pendingConnections.removeValue(pendingDisconnections.get(i), true)) {
				System.out.println("Client removed from matchmaking list");
			}
		}
		
		pendingDisconnections.clear();
	}
	
	private final void processDeadGames() {
		final Array<GameConnection> gameConnections = this.gameConnections;
		final long currentTime = System.currentTimeMillis();
		
		for(int i = 0; i < gameConnections.size; i += 1) {
			final GameConnection gameConnection = gameConnections.get(i);
			boolean dead = false;
			
			final long impulseDelay = currentTime - gameConnection.lastImpulseTime;
			
			if(impulseDelay > Constants.DEAD_GAME_TIME) {
				dead = true;
				System.out.println("Dead game removed because of time delay");
			}
			else if((gameConnection.player1 == null || !gameConnection.player1.isConnected()) &&
					(gameConnection.player2 == null || !gameConnection.player2.isConnected())) {
				
				dead = true;
				System.out.println("Dead game removed because both players are no longer connected");
			}
			
			if(dead) {
				if(gameConnection.player1 != null && gameConnection.player1.isConnected()) {
					gameConnection.player1.getEndPoint().stop();
				}
				if(gameConnection.player2 != null && gameConnection.player2.isConnected()) {
					gameConnection.player2.getEndPoint().stop();
				}
				
				gameConnections.removeIndex(i);
				i -= 1;
				
				System.out.println("Game count after dead game removal: " + gameConnections.size);
			}
		}
	}
	
	private final void setupPlayer(final Player player,
								   final Game game,
								   final MapLocation startLocation) {
		
		player.game = game;
		player.applyAppearance(assets);
		
		game.players.add(player);
		game.physicalObjects.add(player);
		
		player.x = startLocation.x + startLocation.width / 2f;
		player.y = startLocation.y + 2f;
	}
	
	private final void processNewGames() {
		final GameAssets assets = this.assets;
		final Map map = this.map;
		
		final MapLocation player1StartLocation = map.findLocationByName("p1_start");
		final MapLocation player2StartLocation = map.findLocationByName("p2_start");
		
		final int leftOver = 0;// XXX pendingConnections.size % 2;
		int n = pendingConnections.size - leftOver;
		
		for(int i = 0; i < n; i += 1) {//XXX 2) {
			final GameServerConnection connection1 = pendingConnections.get(i);
			// XXX final GameServerConnection connection2 = pendingConnections.get(i + 1);

			final Game game = new Game(map, assets);
			
			final GameServerConnection player1;
			final GameServerConnection player2;
			
			if(MathUtils.randomBoolean()) {
				player1 = connection1;
				player2 = null; // XXX connection2;
			}
			else {
				player1 = null; // XXX connection2;
				player2 = connection1;
			}
			
			final GameConnection gameConnection = new GameConnection(game, player1, player2);
			gameConnections.add(gameConnection);
			
			System.out.println("New game connection added!");
			System.out.println("Game count after new game: " + gameConnections.size);
			
			pendingConnections.removeIndex(i);
			// XXX pendingConnections.removeIndex(i + 1);
			
			i -= 1; // XXX i -= 2;
			n -= 1; // XXX n -= 2;
			
			if(player1 != null) {
				setupPlayer(player1.player, game, player1StartLocation);
			}
			if(player2 != null) {
				setupPlayer(player2.player, game, player2StartLocation);
			}
			
			if(player1 != null && player1.isConnected()) {
				server.sendToTCP(player1.getID(), fillClientAssignmentMessage(new ClientAssignmentMessage(), player1.player));
			}
			if(player2 != null && player2.isConnected()) {
				server.sendToTCP(player2.getID(), fillClientAssignmentMessage(new ClientAssignmentMessage(), player2.player));
			}
		}
	}
	
	private final void processGames(final float deltaTime) {
		final Map map = this.map;
		final long currentTime = System.currentTimeMillis();
		final Array<GameConnection> gameConnections = this.gameConnections;
		
		for(int i = 0; i < gameConnections.size; i += 1) {
			final GameConnection gameConnection = gameConnections.get(i);
			
			worldUpdate.update(deltaTime, gameConnection.game, map);
			
			final WorldStateMessage worldState = new WorldStateMessage();
			worldState.messageTime = currentTime;
			worldState.players = new PlayerMessage[gameConnection.player2 == null || gameConnection.player1 == null ? 1 : 2];
			
			int idx = 0;
			
			if(gameConnection.player1 != null) {
				worldState.players[idx++] = fillPlayerMessage(new PlayerMessage(), gameConnection.player1.player);
			}
			if(gameConnection.player2 != null) {
				worldState.players[idx++] = fillPlayerMessage(new PlayerMessage(), gameConnection.player2.player);
			}
			
			final Array<Spear> spears = gameConnection.game.spears;
			worldState.spears = new SpearMessage[spears.size];
			
			for(int ii = 0; ii < spears.size; ii += 1) {
				worldState.spears[ii] = fillSpearMessage(new SpearMessage(), spears.get(ii));
			}
			
			if(gameConnection.player1 != null && gameConnection.player1.isConnected()) {
				server.sendToUDP(gameConnection.player1.getID(), worldState);
			}
			if(gameConnection.player2 != null && gameConnection.player2.isConnected()) {
				server.sendToUDP(gameConnection.player2.getID(), worldState);
			}
		}
	}
	
	public void update(final float deltaTime) {
		processImpulses();
		processDisconnections();
		processDeadGames();
		processNewGames();
		processGames(deltaTime);
	}
	
	private PlayerMessage fillPlayerMessage(final PlayerMessage message,
								   			final Player player) {
		
		fillObjectMessage(message, player);
		message.flip = player.flip;
		message.requestsAttack = player.requestsAttack;
		message.attackX = player.attackX;
		message.attackY = player.attackY;
		message.hasWeapon = player.hasWeapon;
		message.ownSpearUid = player.ownSpearUid;
		message.ignoreOwnSpear = player.ignoreOwnSpear;
		
		return message;
	}
	
	private SpearMessage fillSpearMessage(final SpearMessage message,
								  		  final Spear spear) {
		
		fillObjectMessage(message, spear);
		message.lastRotation = spear.lastRotation;
		
		return message;
	}
	
	private void fillObjectMessage(final ObjectMessage message,
								   final PhysicalObject object) {
		
		message.isJumping = object.isInAir;
		message.movementDirection = (byte) object.movementDirection.id;
		message.uid = object.uid;
		message.requestsJump = object.requestsJump;
		message.velocityX = object.velocityX;
		message.velocityY = object.velocityY;
		message.x = object.x;
		message.y = object.y;
	}

	private ClientAssignmentMessage fillClientAssignmentMessage(final ClientAssignmentMessage message,
																final Player player) {

		message.playerUid = player.uid;
		message.x = player.x;
		message.y = player.y;
		
		return message;
	}
	
}
