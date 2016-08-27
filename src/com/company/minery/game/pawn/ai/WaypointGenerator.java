package com.company.minery.game.pawn.ai;

import com.company.minery.game.map.Map;
import com.company.minery.game.map.Tiles;
import com.company.minery.game.pawn.Pawn;
import com.company.minery.utils.JumpUtil;

public final class WaypointGenerator {
	
	private Tiles tiles;
	private byte[] byteTiles;
	private int mapWidth;
	private int mapHeight;
	private float tileWidth;
	private float tileHeight;
	private float pawnWidth;
	private float pawnHeight;
	private int pawnWidthInTiles;
	private int pawnHeightInTiles;
	private int clipX;
	private int clipY;
	private int clipWidth;
	private int clipHeight;
	private Waypoint[] out;
	private PhysicsInfo physicsInfo;
	private Pawn pawn;
	private Map map;
	private int runId;
	
	public void generate(final Map map,
						 final Pawn pawn,
						 final PhysicsInfo physicsInfo,
						 final int runId,
						 final int clipX,
						 final int clipY,
						 final int clipWidth,
						 final int clipHeight,
						 final Waypoint[] out) {
		
		this.map = map;
		this.pawn = pawn;
		this.physicsInfo = physicsInfo;
		this.runId = runId;
		this.clipX = clipX;
		this.clipY = clipY;
		this.clipWidth = clipWidth;
		this.clipHeight = clipHeight;
		this.out = out;
		
		final Tiles tiles = map.mainLayer.tiles;
		final byte[] byteTiles = tiles.tiles;
		final int mapWidth = tiles.width;
		final int mapHeight = tiles.height;
		final float tileWidth = map.tileWidth();
		final float tileHeight = map.tileHeight();
		final float pawnWidth = pawn.width();
		final float pawnHeight = pawn.height();
		final int pawnWidthInTiles = (int)(pawnWidth / tileWidth) + (pawnWidth % tileWidth == 0 ? 0 : 1);
		final int pawnHeightInTiles = (int)(pawnHeight / tileHeight) + (pawnHeight % tileHeight == 0 ? 0 : 1);
		
		this.tiles = tiles;
		this.byteTiles = byteTiles;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.pawnWidth = pawnWidth;
		this.pawnHeight = pawnHeight;
		this.pawnWidthInTiles = pawnWidthInTiles;
		this.pawnHeightInTiles = pawnHeightInTiles;
		
		final int nx = clipX + clipWidth;
		final int ny = clipY + clipHeight - 1;
		
		for(int iy = clipY; iy < ny; iy += 1) {
			final int initialIIY = iy + 1;
			final int nny = iy + pawnHeightInTiles;
			
			if(nny > mapHeight) {
				break;
			}
			
			for(int ix = clipX; ix < nx; ix += 1) {
				if(byteTiles[iy * mapWidth + ix] != -128) {
					int iix = ix - pawnWidthInTiles + 1;
					final int nnx = ix + 1;
				
					if(iix < 0) {
						continue;
					}
					
					for(; iix < nnx; iix += 1) {
						final int nnnx = iix + pawnWidthInTiles;
						
						if(nnnx > mapWidth) {
							break;
						}
						
						final boolean collides = containsSolidTiles(iix, initialIIY, pawnWidthInTiles, pawnHeightInTiles);
						
						if(!collides) {
							final Waypoint waypoint = out[(iy + 1 - clipY) * clipWidth + (ix - clipX)];
							
							waypoint.runId = runId;
							waypoint.connections.size = 0;
							waypoint.connectionTypes.size = 0;
							waypoint.ground = true;
							
							break;
						}
					}
				}
			}
		}
		
		for(int iy = 0; iy < clipHeight; iy += 1) {
			for(int ix = 0; ix < clipWidth; ix += 1) {
				final int index = iy * clipWidth + ix;
				final Waypoint waypoint = out[index];
				
				// Check if waypoint is active
				if(waypoint.runId != runId || !waypoint.ground) {
					continue;
				}
				
				// Limit nnx
				final int nnx;
				{
					final int tmpNnx = pawnWidthInTiles + 1;
					if(ix + tmpNnx > clipWidth) {
						nnx = clipWidth - ix;
					}
					else {
						nnx = tmpNnx;
					}
				}
				
				// Check for horizontally connected tiles
				for(int iix = 1; iix < nnx; iix += 1) {
					final Waypoint horizontalNeighbor = out[index + iix];
					
					if(horizontalNeighbor.runId == runId) {
						waypoint.connections.add(horizontalNeighbor);
						waypoint.connectionTypes.add(Waypoint.ConnectionType.Walk);
						
						horizontalNeighbor.connections.add(waypoint);
						horizontalNeighbor.connectionTypes.add(Waypoint.ConnectionType.Walk);
						
						break;
					}
				}
				
				if(iy > 0) {
					if(ix + pawnWidthInTiles < clipWidth) {
						if(!containsSolidTiles(clipX + ix + 1, clipY + iy, pawnWidthInTiles, pawnHeightInTiles) &&
						   !containsSolidTilesH(clipX + ix + 1, clipY + iy - 1, pawnWidthInTiles)) {
							
							// Setup right edge waypoint
							final Waypoint edgeWaypoint = out[index + 1];
							edgeWaypoint.ground = false;
							
							if(edgeWaypoint.runId != runId) {
								edgeWaypoint.connections.size = 0;
								edgeWaypoint.connectionTypes.size = 0;
								edgeWaypoint.runId = runId;
							}
							
							waypoint.connections.add(edgeWaypoint);
							waypoint.connectionTypes.add(Waypoint.ConnectionType.Walk);
							
							processFalls(edgeWaypoint, 0f, 0f);
						}
					}
					
					if(ix - pawnWidthInTiles >= 0) {
						if(!containsSolidTiles(clipX + ix - pawnWidthInTiles, clipY + iy, pawnWidthInTiles, pawnHeightInTiles) &&
						   !containsSolidTilesH(clipX + ix - pawnWidthInTiles, clipY + iy - 1, pawnWidthInTiles)) {
							
							// Setup left edge waypoints
							Waypoint lastEdgeWaypoint = waypoint;
							{
								for(int i = 0; i < pawnWidthInTiles; i += 1) {
									final Waypoint edgeWaypoint = out[index - i - 1];
									edgeWaypoint.ground = false;
									
									if(edgeWaypoint.runId != runId) {
										edgeWaypoint.connections.size = 0;
										edgeWaypoint.connectionTypes.size = 0;
										edgeWaypoint.runId = runId;
									}
									
									lastEdgeWaypoint.connections.add(edgeWaypoint);
									lastEdgeWaypoint.connectionTypes.add(Waypoint.ConnectionType.Walk);
									
									if(i < pawnWidthInTiles - 1) {
										edgeWaypoint.connections.add(lastEdgeWaypoint);
										edgeWaypoint.connectionTypes.add(Waypoint.ConnectionType.Walk);
									}
									
									lastEdgeWaypoint = edgeWaypoint;
								}
							}
							
							processFalls(lastEdgeWaypoint, 0f, 0f);
						}
					}
				}
			}
		}
	}
	
	private boolean containsSolidTilesH(final int x, 
									    final int y, 
									    final int width) {
		
		final byte[] byteTiles = this.byteTiles;
		final int mapWidth = this.mapWidth;
		
		final int nx = x + width;
		
		for(int ix = x; ix < nx; ix += 1) {
			if(byteTiles[y * mapWidth + ix] != -128) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean containsSolidTilesV(final int x, 
										final int y, 
										final int height) {
		
		final byte[] byteTiles = this.byteTiles;
		final int mapWidth = this.mapWidth;
		
		final int ny = y + height;
		
		for(int iy = y; iy < ny; iy += 1) {
			if(byteTiles[iy * mapWidth + x] != -128) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean containsSolidTiles(final int x, 
								   	   final int y, 
								   	   final int width, 
								   	   final int height) {
		
		final byte[] byteTiles = this.byteTiles;
		final int mapWidth = this.mapWidth;
		
		final int nx = x + width;
		final int ny = y + height;
		
		for(int iy = y; iy < ny; iy += 1) {
			for(int ix = x; ix < nx; ix += 1) {
				if(byteTiles[iy * mapWidth + ix] != -128) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void processFalls(final Waypoint startWaypoint,
							  final float totalFallTime,
							  final float currentX) {
		
		final int pawnWidthInTiles = this.pawnWidthInTiles;
		final int x = startWaypoint.x;
		final int y = startWaypoint.y;

		if(y > 0) {
			final float velocityYAtTime = JumpUtil.speedAtTime(0, -physicsInfo.maxVelocityY, totalFallTime, physicsInfo.gravity);
			final float currentFallTime = JumpUtil.fallDuration(
					velocityYAtTime, 
					0, 
					tileHeight, 
					physicsInfo.gravity);
			
			final float nextTotalFallTime = totalFallTime + currentFallTime;
			
			final float possibleTranslation = (int)(physicsInfo.maxVelocityX * currentFallTime);
			final int tileX = (int)(currentX / tileWidth);
			
			// Mark the tiles to the bottom of fall position
			if(!containsSolidTilesH(clipX + x, clipY + y - 1, pawnWidthInTiles)) {
				final Waypoint dstWaypoint = out[(y - 1) * clipWidth + x];
				
				if(markFallWaypoint(startWaypoint, dstWaypoint) && !dstWaypoint.ground) {
					processFalls(dstWaypoint, nextTotalFallTime, currentX);
				}
			}
			
			// Translate to left
			final int translateToLeft;
			{
				final int newTileX = (int)((currentX - possibleTranslation) / tileWidth);
				final int diff = Math.abs(newTileX - tileX);
				
				if(x - diff < 0) {
					translateToLeft = x;
				}
				else {
					translateToLeft = diff;
				}
			}
			
			for(int i = 1; i <= translateToLeft; i += 1) {
				if(!containsSolidTiles(clipX + x - i, clipY + y - 1, pawnWidthInTiles, pawnHeightInTiles)) {
					final Waypoint dstWaypoint = out[(y - 1) * clipWidth + (x - i)];
					
					if(markFallWaypoint(startWaypoint, dstWaypoint) && !dstWaypoint.ground) {
						processFalls(dstWaypoint, nextTotalFallTime, currentX - tileWidth * i - (i == translateToLeft ? possibleTranslation % tileWidth : 0f));
					}
				}
				else {
					break;
				}
			}
			
			// Translate to right
			final int translateToRight;
			{
				final int newTileX = (int)((currentX + possibleTranslation) / tileWidth);
				final int diff = Math.abs(newTileX - tileX);
				
				if(x + diff >= clipWidth) {
					translateToRight = clipWidth - x - 1;
				} 
				else {
					translateToRight = diff;
				}
			}
			
			for(int i = 1; i <= translateToRight; i += 1) {
				if(!containsSolidTiles(clipX + x + i, clipY + y - 1, pawnWidthInTiles, pawnHeightInTiles)) {
					final Waypoint dstWaypoint = out[(y - 1) * clipWidth + (x + i)];
					
					if(markFallWaypoint(startWaypoint, dstWaypoint) && !dstWaypoint.ground) {
						processFalls(dstWaypoint, nextTotalFallTime,  currentX + tileWidth * i + (i == translateToRight ? possibleTranslation % tileWidth : 0f));
					}
				}
				else {
					break;
				}
			}
		}
	}
	
	private boolean markFallWaypoint(final Waypoint startWaypoint, 
								  	 final Waypoint dstWaypoint) {
		
		if(dstWaypoint.runId != runId) {
			dstWaypoint.runId = runId;
			dstWaypoint.connections.size = 0;
			dstWaypoint.connectionTypes.size = 0;
			dstWaypoint.ground = false;
			startWaypoint.connections.add(dstWaypoint);
			startWaypoint.connectionTypes.add(Waypoint.ConnectionType.Fall);
			
			return true;
		}
		else {
			final int index = startWaypoint.connections.indexOf(dstWaypoint, true);
			
			if(index == -1) {
				startWaypoint.connections.add(dstWaypoint);
				startWaypoint.connectionTypes.add(Waypoint.ConnectionType.Fall);
				
				return true;
			}
		}
		
		return false;
	}
	
}
