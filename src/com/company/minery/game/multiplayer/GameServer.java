package com.company.minery.game.multiplayer;

import com.badlogic.gdx.utils.Array;
import com.company.minery.Constants;
import com.company.minery.game.Game;
import com.company.minery.game.GameUpdate;
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
import com.company.minery.utils.AssetResolution;
import com.company.minery.utils.ThreadSafeArray;
import com.company.minery.utils.kryonet.Connection;
import com.company.minery.utils.kryonet.Listener;
import com.company.minery.utils.kryonet.Server;

public final class GameServer implements GameEndpoint {

	/*private static class Message {
		public GameHostConnection connection;
		public Object object;
	}
	
	private final Pool<Message> messagePool = new Pool<Message>() {
		@Override
		protected Message newObject() {
			return new Message();
		}
	};*/
	private final Game game;
	private final GameUpdate worldUpdate = new GameUpdate(this);
	private final Server server;
	
	private final ThreadSafeArray<Connection> mailboxConnections = new ThreadSafeArray<Connection>();
	private final ThreadSafeArray<Object> mailboxObjects = new ThreadSafeArray<Object>();
	
	//private final Array<Message> mailbox = new Array<Message>();
	
	public GameServer(final Game game) {
		this.game = game;
		
		server = new Server() {
			@Override
			protected Connection newConnection() {
				final Player player = new Player(false);
				player.applyAppearance(game.assets);
				game.players.add(player);
				game.currentMap().physicalObjects.add(player);
				return new GameServerConnection(player);
			}
		};
		
		server.addListener(new Listener() {
			@Override
			public void connected(final Connection connection) {
				System.out.println("connected");
				
				game.message = game.assets.fightLabel;
				game.messageTimer = 0;
				
				final MapLocation startLocation = game.currentMap().findLocationByName("p2_start");
				
				final ClientAssignmentMessage clientAssignmentMessage = new ClientAssignmentMessage();
				clientAssignmentMessage.mapId = 0;
				clientAssignmentMessage.x = startLocation.x + startLocation.width / 2f;
				clientAssignmentMessage.y = startLocation.y;
				clientAssignmentMessage.playerUidAssignment = ((GameServerConnection) connection).player.uid;
				clientAssignmentMessage.scale = game.assets.resolution.calcScale();
				
				final Player player = ((GameServerConnection) connection).player;
				player.x = clientAssignmentMessage.x;
				player.y = clientAssignmentMessage.y;
				
				server.sendToTCP(connection.getID(), clientAssignmentMessage);
			}
			
			@Override
			public void disconnected(final Connection connection) {
				System.out.println("disconnected");
			}
			
			@Override
			public void received(final Connection connection, 
								 final Object object) {

				synchronized(mailboxConnections) {
					mailboxConnections.add(connection);
				}
				synchronized(mailboxObjects) {
					mailboxObjects.add(object);
				}
				/*Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						final Message message = messagePool.obtain();
						message.connection = (GameHostConnection) connection;
						message.object = object;
						mailbox.add(message);
					}
				});*/
			}
			
			@Override
			public void idle(final Connection connection) {
			}
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
		}
	}
	
	@Override
	public void end() {
		server.stop();
	}
	
	@Override
	public void update(final float deltaTime) {
		{
			final float localScale = game.assets.resolution.calcScale();
			final ThreadSafeArray<Connection> mailboxConnections = this.mailboxConnections;
			final ThreadSafeArray<Object> mailboxObjects = this.mailboxObjects;
			final int n = mailboxConnections.size();
			
			for(int i = 0; i < n; i += 1) {
				final GameServerConnection connection = (GameServerConnection)(mailboxConnections.get(i));
				final Object object = mailboxObjects.get(i);
				
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
							final float scale = localScale / impulse.scale;
							connection.player.onAttackPressed(impulse.attackX * scale, impulse.attackY * scale);
						}
					}
				}
			}

			synchronized(mailboxConnections) {
				mailboxConnections.clear();
			}
			synchronized(mailboxObjects) {
				mailboxObjects.clear();
			}
		}
	
		worldUpdate.update(deltaTime, game);
		
		if(server.getConnections().length > 0) {
			final WorldStateMessage worldState = new WorldStateMessage();
			worldState.messageTime = System.currentTimeMillis();
			worldState.scale = game.assets.resolution.calcScale();
			
			final Array<Player> players = game.players;
			final Array<Spear> spears = game.spears;
			
			final PlayerMessage[] playerMessages = new PlayerMessage[players.size];
			final SpearMessage[] spearMessages = new SpearMessage[spears.size];
			
			for(int i = 0; i < players.size; i += 1) {
				final Player player = players.get(i);
				final PlayerMessage message = new PlayerMessage();
				
				fillObjectMessage(message, player);
				message.flip = player.flip;
				message.requestsAttack = player.requestsAttack;
				message.attackX = player.attackX;
				message.attackY = player.attackY;
				message.hasWeapon = player.hasWeapon;
				message.ownSpearUid = player.ownSpearUid;
				message.ignoreOwnSpear = player.ignoreOwnSpear;
				
				playerMessages[i] = message;
			}
			
			for(int i = 0; i < spears.size; i += 1) {
				final Spear spear = spears.get(i);
				final SpearMessage message = new SpearMessage();
				
				fillObjectMessage(message, spear);
				message.lastRotation = spear.lastRotation;
				
				spearMessages[i] = message;
			}
			
			worldState.players = playerMessages;
			worldState.spears = spearMessages;
			
			server.sendToAllUDP(worldState);
		}
	}
	
	private void fillObjectMessage(final ObjectMessage message, final PhysicalObject object) {
		message.isJumping = object.isInAir;
		message.movementDirection = (byte) object.movementDirection.id;
		message.uid = object.uid;
		message.requestsJump = object.requestsJump;
		message.velocityX = object.velocityX;
		message.velocityY = object.velocityY;
		message.x = object.x;
		message.y = object.y;
	}

}
