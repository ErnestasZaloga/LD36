package com.company.minery.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.company.minery.game.map.Layer;
import com.company.minery.game.map.Map;
import com.company.minery.game.map.Tile;
import com.company.minery.game.map.Tiles;
import com.company.minery.game.multiplayer.GameClient;
import com.company.minery.game.pawn.Lasso;
import com.company.minery.game.pawn.Pawn;
import com.company.minery.game.pawn.Pawn.MovementDirection;
import com.company.minery.game.pawn.ai.Pathfinder;
import com.company.minery.game.pawn.ai.PhysicsInfo;
import com.company.minery.utils.spine.Skeleton;

public final class WorldUpdate {

	private final GameClient gameClient;
	private final Vector2 tmpVector = new Vector2();
	private final Pathfinder pathfinding = new Pathfinder(50, 50);
	private final PhysicsInfo physicsInfo = new PhysicsInfo();
	
	public WorldUpdate(final GameClient gameClient) {
		this.gameClient = gameClient;
	}
	
	private Map map;
	
	public void update(final float deltaTime,
					   final Game game) {

		final Map map = game.currentMap();
		this.map = map;
		
		final float tileWidth = map.tileWidth();
		final float tileHeight = map.tileHeight();
		
		// FIXME HARDCODED!!!
		final float maxFallSpeed = -tileHeight * 10f;
		final float gravity = tileHeight * 10f;
		
		physicsInfo.minVelocityY = -maxFallSpeed;
		physicsInfo.gravity = -gravity;
		
		final float maxLassoVelY = tileHeight * 20f;
		final float maxLassoLength = tileWidth * 7;
		
		final Array<Pawn> pawns = map.pawns;
		final int n = pawns.size;

		final boolean panningEnabled = game.panningEnabled();
		final Vector2 tmpVector = this.tmpVector;
		
		final Layer mainLayer = map.mainLayer;
		final Tiles mainLayerTiles = mainLayer.tiles;
		final byte[] mainLayerByteTiles = mainLayerTiles.tiles;
		final Tile[] mainLayerTileset = mainLayerTiles.tileset;
		final int mainLayerWidth = mainLayerTiles.width;
		final int mainLayerHeight = mainLayerTiles.height;
		
		for(int i = 0; i < n; i += 1) {
			final Pawn pawn = pawns.get(i);
			final Lasso lasso = pawn.lasso;
			
			// ************************************
			// HANDLE LASSO REQUEST
			// ************************************
			if(pawn.requestsLasso) {
				if(lasso.enabled) {
					lasso.hooked = false;
					lasso.enabled = false;
				}
				
				final float lassoTargetX = pawn.lassoTargetX;
				final float lassoTargetY = pawn.lassoTargetY;
				
				pawn.requestsLasso = false;
				
				final float pawnCenterX = pawn.x + pawn.width() / 2f;
				final float pawnCenterY = pawn.y + pawn.height() / 2f;
				
				tmpVector.set(lassoTargetX - pawnCenterX, lassoTargetY - pawnCenterY);
				final float angle = tmpVector.angle() - 90f;
				
				tmpVector.set(0f, maxLassoVelY);
				tmpVector.rotate(angle);
				
				lasso.enabled = true;
				
				lasso.angle = angle - 90f;
				lasso.velocityX = tmpVector.x;
				lasso.velocityY = tmpVector.y;
				lasso.startX = pawnCenterX;
				lasso.startY = pawnCenterY;
				lasso.endX = pawnCenterX;
				lasso.endY = pawnCenterY;
			}
			
			// ************************************
			// UPDATE PAWN
			// ************************************
			{
				//update pawn FlipX
				final MovementDirection dir = pawn.movementDirection;
				final Skeleton skeleton = pawn.skeleton();
				
				if(dir == MovementDirection.Left) {
					skeleton.getRootBone().setFlipX(true);
				} 
				else if(dir == MovementDirection.Right) {
					skeleton.getRootBone().setFlipX(false);
				}
				
				// update pawn jump animation
				boolean jumpStart = pawn.requestsJump;
				boolean inAir = pawn.isInAir;
				jumpStart = jumpStart && !inAir;
				
				if(jumpStart) {
					pawn.animationTimer = 0f;
				}
				
				final float animationTimer = pawn.animationTimer;
				
				if(inAir && pawn.jumpAnimation() != null) {
					pawn.jumpAnimation().mix(
							skeleton, 
							animationTimer, 
							animationTimer + deltaTime, 
							false, 
							null,
							0.3f);
				}
				
				// update pawn idle or run animation if not in air
				if(!inAir) {
					if(!pawn.isRunning) {
						pawn.idleAnimation().apply(
								skeleton, 
								animationTimer, 
								animationTimer + deltaTime, 
								true, 
								null);
					}
					else {
						pawn.runAnimation().apply(
								skeleton, 
								animationTimer, 
								animationTimer + deltaTime, 
								true, 
								null);
					}
				}
				
				pawn.animationTimer = animationTimer + deltaTime;
				skeleton.updateWorldTransform();
			}
			
			// ************************************
			// SET/UPDATE VELOCITY
			// ************************************
			
			final float maxPawnVelocityX = pawn.runSpeedPerc * tileWidth;
			final float maxPawnVelocityY = pawn.jumpHeightPerc * tileHeight;
			{
				pawn.velocityX = pawn.movementDirection.mul * maxPawnVelocityX;
				
				if(pawn.velocityX != 0f) {
					pawn.isRunning = true;
				}
				else {
					pawn.isRunning = false;
				}
				
				if(pawn.requestsJump) {
					pawn.requestsJump = false;
					
					if(!pawn.isInAir) {
						pawn.isJumping = true;
						pawn.isInAir = true;
						pawn.velocityY = maxPawnVelocityY;
					}
				}
				else {
					float velY = pawn.velocityY - gravity * deltaTime;
					
					if(velY < maxFallSpeed) {
						velY = maxFallSpeed;
					}
					pawn.velocityY = velY;
				}
			}
			
			physicsInfo.maxVelocityY = maxPawnVelocityY;
			physicsInfo.maxVelocityX = maxPawnVelocityX;
			
			final float pawnX;
			final float pawnY;
			final float pawnWidth = pawn.width();
			final float pawnHeight = pawn.height();
			
			// ************************************
			// APPLY MOVEMENT/COLLISIONS
			// ************************************
			{
				final float currentX = pawn.x;
				final float currentY = pawn.y;
				
				final float velocityX;
				final float velocityY;
				
				if(lasso.enabled && lasso.hooked) {
					if(tileWidth * 2 >= lasso.length) {
						lasso.hooked = false;
						lasso.enabled = false;
					}
					
					velocityX = pawn.velocityX + lasso.velocityX;
					velocityY = pawn.velocityY + lasso.velocityY;
				}
				else {
					velocityX = pawn.velocityX;
					velocityY = pawn.velocityY;
				}
				
				final float normVelocityX = velocityX * deltaTime;
				final float normVelocityY = velocityY * deltaTime;
				
				final int segmentsX = 8; // TODO: calculate by the movement amount
				final int segmentsY = 8; // TODO: calculate by the movement amount
				final int segments = Math.max(segmentsX, segmentsY);
				
				float xMod = 1f;
				float yMod = 1f;
				
				{
					final float segmentStep = 1f / segments;
					for(int ii = segments; ii >= 1; ii -= 1) {
						final float mod = (segmentStep * ii);
						xMod = mod;
						yMod = mod;
						
						final float x = currentX + normVelocityX * xMod;
						final float y = currentY + normVelocityY * yMod;
						
						if(!checkPawnCollision(pawn, x, y, x + pawnWidth, y + pawnHeight, map)) {
							break;
						}
						else {
							xMod = 0f;
							yMod = 0f;
						}
					}
				}
				
				{
					final float y = currentY + normVelocityY * yMod;
					final float segmentStep = 1f / segmentsX;
					for(int ii = segments; ii >= 1; ii -= 1) {
						final float mod = (segmentStep * ii);
						xMod = mod;
						
						final float x = currentX + normVelocityX * xMod;
						
						if(!checkPawnCollision(pawn, x, y, x + pawnWidth, y + pawnHeight, map)) {
							break;
						}
						else {
							xMod = 0f;
						}
					}
				}
				
				{
					final float x = currentX + normVelocityX * xMod;
					final float segmentStep = 1f / segmentsY;
					for(int ii = segments; ii >= 1; ii -= 1) {
						final float mod = (segmentStep * ii);
						yMod = mod;
						
						final float y = currentY + normVelocityY * yMod;
						
						if(!checkPawnCollision(pawn, x, y, x + pawnWidth, y + pawnHeight, map)) {
							pawn.isInAir = true;
							break;
						}
						else {
							yMod = 0f;
						}
					}
				}
				
				pawnX = currentX + normVelocityX * xMod;
				pawnY = currentY + normVelocityY * yMod;
				
				pawn.x = pawnX;
				pawn.y = pawnY;

				// If hits sides
				if(xMod == 0) {
					pawn.isRunning = false;
				}
				
				// If hits the ground or top.
				if(yMod == 0) {
					// If velocity is less than 0 this means that pawn has hit the ground.
					if(normVelocityY < 0f) {
						pawn.isInAir = false;
					}
					pawn.velocityY = 0f;
				}
				
				if(pawn.velocityY <= 0f) {
					pawn.isJumping = false;
				}
			}
			
			physicsInfo.currentVelocityY = pawn.velocityY;
			
			// ************************************
			// HANDLE AI
			// ************************************
			if(pawn.pathfindingEnabled) {
				final int currentTileX = (int)(pawnX / tileWidth);
				final int currentTileY = (int)(pawnY / tileHeight);

				// Find destination coordinates.
				final int destinationX = (int)(pawn.pathfindingMapX / tileWidth);
				final int destinationY = (int)(pawn.pathfindingMapY / tileHeight);
				
				final Pathfinder.PathTile currentPathTile = pawn.currentPathTile;
				final boolean positionChanged = (currentPathTile.x != currentTileX || currentPathTile.y != currentTileY);
				
				// If reached next tile position or new path was requested.
				if(!pawn.initialPathFound || 
				   positionChanged) {
					
					// Find the path(if any).
					pawn.pathfindingEnabled = pathfinding.findPath(map, pawn, physicsInfo, currentTileX, currentTileY, destinationX, destinationY);
					
					if(currentPathTile.x == destinationX && 
					   currentPathTile.y == destinationY) {
						
						pawn.x = pawn.pathfindingMapX;
					}
					
					// Mark initial path as found.
					pawn.initialPathFound = true;

					// Set the required horizontal direction by the path tile.
					pawn.movementDirection = currentPathTile.direction;
					
					// Make a jump request if the tile is a jump point.
					if(currentPathTile.jump) {
						pawn.requestsJump = true;
					}
					
					if(pawn.pathfindingEnabled) {
						System.out.println("path found");
					}
					else {
						System.out.println("path not found");
					}
				}
			}
			
			// ************************************
			// HANDLE MINING
			// ************************************
			if(pawn.requestsMining && !panningEnabled) {
				pawn.requestsMining = false;
				
				final int miningTileX = (int)(pawn.miningTargetX / tileWidth);
				final int miningTileY = (int)(pawn.miningTargetY / tileHeight);

				final int pawnTileLeft = (int)(pawnX / tileWidth);
				final int pawnTileBottom = (int)(pawnY / tileHeight);
				final int pawnTileRight = (int)((pawnX + pawnWidth) / tileWidth);
				final int pawnTileTop = (int)((pawnY + pawnHeight) / tileHeight);

				if((miningTileX <= pawnTileRight + 1 &&
				    miningTileY <= pawnTileTop + 1 &&
				    miningTileX >= pawnTileLeft - 1 &&
				    miningTileY >= pawnTileBottom - 1)) {

					final int tileMapIndex = miningTileY * mainLayerWidth + miningTileX;
					final int tileIndex = mainLayerByteTiles[tileMapIndex] + 127;
					
					if(tileIndex != -1 && mainLayerTileset[tileIndex].minable) {
						mainLayerByteTiles[tileMapIndex] = -128;
					}
				}
			}
			
			// ************************************
			// HANDLE LASSO
			// ************************************
			if(lasso.enabled) {
				lasso.startX = pawnX + pawnWidth / 2f;
				lasso.startY = pawnY + pawnHeight / 2f;
				
				final float startX = lasso.startX;
				final float startY = lasso.startY;
				
				if(!lasso.hooked) {
					final float velocityX = lasso.velocityX;
					final float velocityY = lasso.velocityY;
					final float normVelocityX = velocityX * deltaTime;
					final float normVelocityY = velocityY * deltaTime;
					
					final float endX = lasso.endX + normVelocityX;
					final float endY = lasso.endY + normVelocityY;
					
					lasso.endX = endX;
					lasso.endY = endY;
					lasso.calculateLength();
					
					tmpVector.x = endX - startX;
					tmpVector.y = endY - startY;
					
					lasso.angle = tmpVector.angle();
					
					// Check collision
					final int headTileX = (int)(endX / tileWidth);
					final int headTileY = (int)(endY / tileHeight);
					final Tiles tiles = map.mainLayer.tiles;
					
					if(tiles.tiles[headTileY * tiles.width + headTileX] >= 0) {
						lasso.hooked = true;
					}
					else {
						if(lasso.length > maxLassoLength) {
							lasso.enabled = false;
						}
					}
				}
				else {
					final float endX = lasso.endX;
					final float endY = lasso.endY;
					
					lasso.calculateLength();
					
					tmpVector.x = endX - startX;
					tmpVector.y = endY - startY;
					
					final float angle = tmpVector.angle();
					lasso.angle = angle;
					
					tmpVector.set(0f, maxLassoVelY);
					tmpVector.rotate(angle - 90f);
					
					lasso.velocityX = tmpVector.x;
					lasso.velocityY = tmpVector.y;
				}
			}
		}
	}
	
	private boolean checkPawnCollision(final Pawn pawn, 
									   final float pawnX, 
									   final float pawnY, 
									   final float pawnRight, 
									   final float pawnTop,
									   final Map map) {
		
		final float tileWidth = map.tileWidth();
		final float tileHeight = map.tileHeight();
		
		final Tiles tiles = map.mainLayer.tiles;
		final byte[] tileIndexes = tiles.tiles;
		final int tilesWidth = tiles.width;
		
		final int tileLeft = (int)(pawnX / tileWidth);
		final int tileBottom = (int)(pawnY / tileHeight);
		final int tileRight = (int)(pawnRight / tileWidth);
		final int tileTop = (int)(pawnTop / tileHeight);
		
		for(int yi = tileBottom; yi <= tileTop; yi += 1) {
			for(int xi = tileLeft; xi <= tileRight; xi += 1) {
				final byte tileIndex = tileIndexes[yi * tilesWidth + xi];
				
				if(tileIndex == -128) {
					continue;
				}
				
				return true;
			}
		}
		
		return false;
	}
	
}
