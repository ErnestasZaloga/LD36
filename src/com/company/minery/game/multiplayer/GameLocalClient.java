package com.company.minery.game.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.company.minery.Constants;
import com.company.minery.game.Game;
import com.company.minery.game.WorldUpdate;
import com.company.minery.game.multiplayer.messages.ClientAssignmentMessage;
import com.company.minery.game.multiplayer.messages.ImpulseMessage;
import com.company.minery.game.multiplayer.messages.PawnMessage;
import com.company.minery.game.multiplayer.messages.WorldStateMessage;
import com.company.minery.game.pawn.Lasso;
import com.company.minery.game.pawn.Player;
import com.company.minery.utils.AssetResolution;
import com.company.minery.utils.ThreadSafeArray;
import com.company.minery.utils.kryonet.Connection;
import com.company.minery.utils.kryonet.Listener;
import com.company.minery.utils.kryonet.Server;

public final class GameLocalClient implements GameClient {

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
	private final WorldUpdate worldUpdate = new WorldUpdate(this);
	private final Server server;
	
	private final ThreadSafeArray<Connection> mailboxConnections = new ThreadSafeArray<Connection>();
	private final ThreadSafeArray<Object> mailboxObjects = new ThreadSafeArray<Object>();
	
	//private final Array<Message> mailbox = new Array<Message>();
	
	public GameLocalClient(final Game game) {
		this.game = game;
		
		server = new Server() {
			@Override
			protected Connection newConnection() {
				final Player player = new Player(false);
				player.applySkeletonData(game.assets.testSkelData());
				game.players.add(player);
				game.currentMap().pawns.add(player);
				return new GameHostConnection(player);
			}
		};
		
		server.addListener(new Listener() {
			@Override
			public void connected(final Connection connection) {
				System.out.println("connected");

				final AssetResolution resolution = game.assets.resolution();
				int resolutionIndex = 0;
				
				{
					final AssetResolution[] resolutions = Constants.RESOLUTION_LIST;
					final int n = resolutions.length;
					
					for(int i = 0; i < n; i += 1) {
						if(resolutions[i] == resolution) {
							resolutionIndex = i;
							break;
						}
					}
				}
				
				
				final Player localPlayer = game.localPlayer();
				final ClientAssignmentMessage clientAssignmentMessage = new ClientAssignmentMessage();
				clientAssignmentMessage.mapId = 0;
				clientAssignmentMessage.x = localPlayer.x;
				clientAssignmentMessage.y = localPlayer.y;
				clientAssignmentMessage.playerUidAssignment = ((GameHostConnection) connection).player.uid;
				clientAssignmentMessage.resolutionIndex = (byte)resolutionIndex;
				
				final Player player = ((GameHostConnection) connection).player;
				player.x = localPlayer.x;
				player.y = localPlayer.y;
				
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
			final ThreadSafeArray<Connection> mailboxConnections = this.mailboxConnections;
			final ThreadSafeArray<Object> mailboxObjects = this.mailboxObjects;
			final int n = mailboxConnections.size();
			
			for(int i = 0; i < n; i += 1) {
				final GameHostConnection connection = (GameHostConnection)(mailboxConnections.get(i));
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
			final AssetResolution resolution = game.assets.resolution();
			int resolutionIndex = 0;
			
			{
				final AssetResolution[] resolutions = Constants.RESOLUTION_LIST;
				final int n = resolutions.length;
				
				for(int i = 0; i < n; i += 1) {
					if(resolutions[i] == resolution) {
						resolutionIndex = i;
						break;
					}
				}
			}
			
			final WorldStateMessage worldState = new WorldStateMessage();
			worldState.messageTime = System.currentTimeMillis();
			worldState.resolutionIndex = (byte)resolutionIndex;
			
			final Array<Player> players = game.players;
			final PawnMessage[] pawnMessages = new PawnMessage[players.size];
			final int n = players.size;
			
			for(int i = 0; i < n; i += 1) {
				final Player player = players.get(i);
				final Lasso lasso = player.lasso;
				final PawnMessage message = new PawnMessage();
				
				message.animationTimer = player.animationTimer;
				message.isJumping = player.isInAir;
				message.lassoEnabled = lasso.enabled;
				message.lassoEndX = lasso.endX;
				message.lassoEndY = lasso.endY;
				message.lassoHooked = lasso.hooked;
				message.lassoStartX = lasso.startX;
				message.lassoStartY = lasso.startY;
				message.lassoTargetX = player.lassoTargetX;
				message.lassoTargetY = player.lassoTargetY;
				message.lassoVelocityX = lasso.velocityX;
				message.lassoVelocityY = lasso.velocityY;
				message.miningTargetX = player.miningTargetX;
				message.miningTargetY = player.miningTargetY;
				message.movementDirection = (byte)player.movementDirection.id;
				message.pawnUid = player.uid;
				message.requestsJump = player.requestsJump;
				message.requestsLasso = player.requestsLasso;
				message.requestsMining = player.requestsMining;
				message.skeletonFlipped = player.skeleton().getFlipX();
				message.velocityX = player.velocityX;
				message.velocityY = player.velocityY;
				message.x = player.x;
				message.y = player.y;
				
				pawnMessages[i] = message;
			}
			
			worldState.pawns = pawnMessages;
			server.sendToAllUDP(worldState);
		}
	}

}
