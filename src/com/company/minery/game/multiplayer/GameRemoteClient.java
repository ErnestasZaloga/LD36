package com.company.minery.game.multiplayer;

import com.badlogic.gdx.utils.Array;
import com.company.minery.Constants;
import com.company.minery.game.Game;
import com.company.minery.game.WorldUpdate;
import com.company.minery.game.multiplayer.messages.ClientAssignmentMessage;
import com.company.minery.game.multiplayer.messages.ImpulseMessage;
import com.company.minery.game.multiplayer.messages.PawnMessage;
import com.company.minery.game.multiplayer.messages.WorldStateMessage;
import com.company.minery.game.pawn.Lasso;
import com.company.minery.game.pawn.Pawn;
import com.company.minery.game.pawn.Pawn.MovementDirection;
import com.company.minery.game.pawn.Player;
import com.company.minery.utils.AssetResolution;
import com.company.minery.utils.kryonet.Client;
import com.company.minery.utils.kryonet.Connection;
import com.company.minery.utils.kryonet.Listener;

public final class GameRemoteClient implements GameClient {

	private final Game game;
	private final Client client;
	private final WorldUpdate worldUpdate = new WorldUpdate(this);
	private final Runnable disconnectCallback;
	private final Array<Object> receivedObjects = new Array<Object>();
	
	public GameRemoteClient(final Game game,
							final Runnable disconnectCallback) {
		
		this.game = game;
		this.disconnectCallback = disconnectCallback;
		
		client = new Client();
		client.addListener(new Listener() {
			@Override
			public void connected(final Connection connection) {
				System.out.println("connected");
			}
			
			@Override
			public void disconnected(final Connection connection) {
				if(disconnectCallback != null) {
					disconnectCallback.run();
				}
			}
			
			@Override
			public void received(final Connection connection, 
								 final Object object) {
				
				receivedObjects.add(object);
			}
			
			@Override
			public void idle(final Connection connection) {
				System.out.println("idle");
			}
		});
		Multiplayer.register(client);
	}
	
	public void begin(final String serverIpAddress, 
					  final int tcpPort, 
					  final int udpPort) {
		
		client.start();
		
		try {
			client.connect(10000, serverIpAddress, tcpPort, udpPort);
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			if(disconnectCallback != null) {
				disconnectCallback.run();
			}
		}
		
		game.currentMap().pawns.clear();
		game.players.clear();
	}
	
	@Override
	public void end() {
		client.close();
	}
	
	@Override
	public void update(final float deltaTime) {
		final boolean requestsJump = game.localPlayer().requestsJump;
		final Pawn.MovementDirection movementDirection = game.localPlayer().movementDirection;
		
		synchronized(this) {
			final Array<Object> receivedObjects = this.receivedObjects;
			final int n = receivedObjects.size;
			
			for(int i = 0; i < n; i += 1) {
				final Object object = receivedObjects.get(i);
				
				if(object instanceof WorldStateMessage) {
					final WorldStateMessage worldState = (WorldStateMessage) object;
					final PawnMessage[] pawns = worldState.pawns;
					final int nn = pawns.length;
					final AssetResolution dataResolution = Constants.RESOLUTION_LIST[worldState.resolutionIndex];
					final float scale = dataResolution.calcScale() / game.assets.resolution().calcScale();
					
					for(int ii = 0; ii < nn; ii += 1) {
						final PawnMessage message = pawns[ii];
						
						boolean found = false;
						final int nnn = game.players.size;
						for(int iii = 0; iii < nnn; iii += 1) {
							final Player player = game.players.get(iii);
							
							if(player.uid == message.pawnUid) {
								setPawnState(player, message, scale);
								found = true;
								break;
							}
						}
						
						if(!found) {
							final Player player = new Player(false, message.pawnUid);
							player.applySkeletonData(game.assets.testSkelData());
							game.players.add(player);
							game.currentMap().pawns.add(player);
							setPawnState(player, message, scale);
						}
					}
					
					// TODO: handle removal
				}
				else if(object instanceof ClientAssignmentMessage) {
					final ClientAssignmentMessage clientAssignment = (ClientAssignmentMessage) object;
					
					// TODO: load map here
					
					final Player localPlayer = new Player(true, clientAssignment.playerUidAssignment);
					localPlayer.applySkeletonData(game.assets.testSkelData());
					game.setLocalPlayer(localPlayer);
					game.inputTranslator().setListener(localPlayer);
					game.currentMap().pawns.add(localPlayer);
					game.players.add(localPlayer);
				
					final AssetResolution dataResolution = Constants.RESOLUTION_LIST[clientAssignment.resolutionIndex];
					final float scale = dataResolution.calcScale() / game.assets.resolution().calcScale();
					
					localPlayer.x = clientAssignment.x * scale;
					localPlayer.y = clientAssignment.y * scale;
				}
			}
			
			receivedObjects.clear();
		}
		
		worldUpdate.update(deltaTime, game);
		
		if(client.isConnected()) {
			final ImpulseMessage impulseMessage = new ImpulseMessage();
			
			impulseMessage.jumpFlag = requestsJump;
			impulseMessage.movementFlag = (byte)movementDirection.id;
			impulseMessage.messageTime = System.currentTimeMillis();
			
			client.sendUDP(impulseMessage);
		}
	}
	
	private void setPawnState(final Pawn pawn, 
							  final PawnMessage message,
							  final float scale) {
		
		final Lasso lasso = pawn.lasso;
		
		pawn.animationTimer = message.animationTimer;
		pawn.requestsJump = message.requestsJump;
		pawn.isInAir = message.isJumping;
		lasso.enabled = message.lassoEnabled;
		lasso.endX = message.lassoEndX;
		lasso.endY = message.lassoEndY;
		lasso.hooked = message.lassoHooked;
		lasso.startX = message.lassoStartX;
		lasso.startY = message.lassoStartY;
		lasso.calculateLength();
		pawn.requestsLasso = message.requestsLasso;
		pawn.lassoTargetX = message.lassoTargetX;
		pawn.lassoTargetY = message.lassoTargetY;
		pawn.velocityX = message.velocityX;
		pawn.velocityY = message.velocityY;
		pawn.requestsMining = message.requestsMining;
		pawn.miningTargetX = message.miningTargetX;
		pawn.miningTargetY = message.miningTargetY;
		pawn.movementDirection = MovementDirection.valueOf(message.movementDirection);
		pawn.velocityX = message.velocityX;
		pawn.velocityY = message.velocityY;
		pawn.x = message.x;
		pawn.y = message.y;
		pawn.skeleton().setFlipX(message.skeletonFlipped);
	}
	
}
