package com.company.minery.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.company.minery.game.map.Layer;
import com.company.minery.game.map.Map;
import com.company.minery.game.map.StaticDecoration;
import com.company.minery.game.map.Tile;
import com.company.minery.game.map.Tiles;
import com.company.minery.game.pawn.Lasso;
import com.company.minery.game.pawn.Pawn;
import com.company.minery.game.pawn.ai.PhysicsInfo;
import com.company.minery.game.pawn.ai.Waypoint;
import com.company.minery.game.pawn.ai.WaypointGenerator;
import com.company.minery.game.takable.RegionTakable;
import com.company.minery.game.takable.SkeletonTakable;
import com.company.minery.game.takable.Takable;
import com.company.minery.utils.JumpUtil;
import com.company.minery.utils.spine.Skeleton;
import com.company.minery.utils.spine.SkeletonRenderer;

public class GameRenderer {

	private int clipX;
	private int clipY;
	
	private final Vector2 tmpVector = new Vector2();
	private final PolygonSpriteBatch batch;
	private final SkeletonRenderer skeletonRenderer;
	
	private final PhysicsInfo physicsInfo = new PhysicsInfo();
	private final WaypointGenerator waypointGenerator = new WaypointGenerator();
	private final int waypointsWidth = 50;
	private final int waypointsHeight = 50;
	private final Waypoint[] waypoints = new Waypoint[waypointsWidth * waypointsHeight];
	private int runId;
	
	public GameRenderer(final PolygonSpriteBatch batch) {
		this.batch = batch;
		skeletonRenderer = new SkeletonRenderer();

		for(int iy = 0; iy < waypointsHeight; iy += 1) {
			for(int ix = 0; ix < waypointsWidth; ix += 1) {
				final Waypoint waypoint = new Waypoint();
				waypoint.x = ix;
				waypoint.y = iy;
				
				waypoints[iy * waypointsWidth + ix] = waypoint;
			}
		}
	}
	
	public void debugUpdate(final Map map) {
		final float tileWidth = map.tileWidth();
		final float tileHeight = map.tileHeight();
		final int mapWidth = map.mainLayer.tiles.width;
		final int mapHeight = map.mainLayer.tiles.height;
		final Pawn pawn = map.pawns.first();
		
		final float maxFallSpeed = -tileHeight * 10f;
		final float gravity = tileHeight * 10f;
		final float maxPawnVelocityX = pawn.runSpeedPerc * tileWidth;
		final float maxPawnVelocityY = pawn.jumpHeightPerc * tileHeight;
		
		physicsInfo.minVelocityY = -maxFallSpeed;
		physicsInfo.gravity = -gravity;
		physicsInfo.maxVelocityY = maxFallSpeed;
		physicsInfo.maxVelocityX = maxPawnVelocityX;
		physicsInfo.currentVelocityY = pawn.velocityY;
		
		runId += 1;
		
		final int pawnTileX = (int)(pawn.x / tileWidth);
		final int pawnTileY = (int)(pawn.y / tileHeight);
		
		clipX = pawnTileX - waypointsWidth / 2;
		clipY = pawnTileY - waypointsHeight / 2;
		
		if(clipX < 0) {
			clipX = 0;
		}
		if(clipY < 0) {
			clipY = 0;
		}
		
		if(clipX + waypointsWidth > mapWidth) {
			clipX = mapWidth - waypointsWidth - 1;
		}
		if(clipY + waypointsHeight > mapHeight) {
			clipY = mapHeight - waypointsHeight - 1;
		}
		
		waypointGenerator.generate(map, pawn, physicsInfo, runId, clipX, clipY, waypointsWidth, waypointsHeight, waypoints);
	}
	
	public void render(final Game game) {
		batch.setColor(1f, 1f, 1f, 1f);
		
		final Vector2 tmpVector = this.tmpVector;
		final float assetScale = game.assets.resolution().calcScale();
		
		final Map map = game.currentMap();
		final float viewX = map.viewX;
		final float viewY = map.viewY;
		
		final float tileWidth = map.tileWidth();
		final float tileHeight = map.tileHeight();
		
		final float screenWidth = Gdx.graphics.getWidth();
		final float screenHeight = Gdx.graphics.getHeight();
		
		final float lassoThickness = tileWidth * 0.05f;
		
		final float cullX = viewX;
		final float cullY = viewY;
		final float cullRight = cullX + screenWidth;
		final float cullTop = cullY + screenHeight;
		
		final int cullTileLeft;
		final int cullTileBottom;
		final int cullTileRight;
		final int cullTileTop;
		
		final Layer mainLayer = map.mainLayer;
	
		final int mapWidth = mainLayer.tiles.width;
		final int mapHeight = mainLayer.tiles.height;

		// ******************************
		// CALCULATE TILE CULLING
		// ******************************
		{
			int rawCullTileLeft = (int)(cullX / tileWidth);
			int rawCullTileBottom = (int)(cullY / tileHeight);
			int rawCullTileRight = (int)(cullRight / tileWidth);
			int rawCullTileTop = (int)(cullTop / tileHeight);
		
			if(rawCullTileLeft < 0) {
				rawCullTileLeft = 0;
			}
			if(rawCullTileBottom < 0) {
				rawCullTileBottom = 0;
			}
			if(rawCullTileRight >= mapWidth) {
				rawCullTileRight = mapWidth - 1;
			}
			if(rawCullTileTop >= mapHeight) {
				rawCullTileTop = mapHeight - 1;
			}
			
			cullTileLeft = rawCullTileLeft;
			cullTileBottom = rawCullTileBottom;
			cullTileRight = rawCullTileRight;
			cullTileTop = rawCullTileTop;
		}
		
		// ******************************
		// RENDER LAYERS BEFORE MAIN
		// ******************************	
		final Layer[] layers = map.layers;
		
		for(int i = 0, n = map.mainLayerIndex; i < n; i++) {
			final Layer layer = layers[i];
			final Tiles tiles = layer.tiles;
			
			if(tiles != null) {
				final int xCoord = tiles.tileXCoord();
				final int yCoord = tiles.tileYCoord();
				
				int drawLeft = xCoord;
				int drawBottom = yCoord;
				int drawRight = drawLeft + tiles.width - 1;
				int drawTop = drawBottom + tiles.height - 1;

				if(drawLeft <= cullTileRight &&
				   drawBottom <= cullTileTop &&
				   drawRight >= cullTileLeft &&
				   drawTop >= cullTileBottom) {
					
					if(drawLeft < cullTileLeft) {
						drawLeft = cullTileLeft;
					}
					
					if(drawBottom < cullTileBottom) {
						drawBottom = cullTileBottom;
					}
					
					if(drawRight > cullTileRight) {
						drawRight = cullTileRight;
					}
					
					if(drawTop > cullTileTop) {
						drawTop = cullTileTop;
					}

					drawLeft -= xCoord;
					drawBottom -= yCoord;
					drawRight -= xCoord;
					drawTop -= yCoord;
					
					final float x = tileWidth * xCoord - viewX;
					final float y = tileHeight * yCoord - viewY;
					
					renderTiles(tiles, x, y, tileWidth, tileHeight, drawLeft, drawBottom, drawRight, drawTop);
				}
			}
			
			final StaticDecoration[] decorations = layer.decorations();
			if(decorations != null) {
				renderDecorations(decorations, 
								  viewX, 
								  viewY, 
								  cullX, 
								  cullY, 
								  cullRight, 
								  cullTop);
			}
		}
		
		// ******************************
		// RENDER MAIN LAYER
		// ******************************
		{
			renderTiles(
					mainLayer.tiles, 
					-viewX, 
					-viewY, 
					tileWidth, 
					tileHeight, 
					cullTileLeft, 
					cullTileBottom,
					cullTileRight,
					cullTileTop);
			
			final StaticDecoration[] decorations = mainLayer.decorations();
			if(decorations != null) {
				renderDecorations(decorations, 
								  viewX, 
								  viewY, 
								  cullX, 
								  cullY, 
								  cullRight, 
								  cullTop);
			}
		}
		
		{
			for(int i = 0; i < waypoints.length; i += 1) {
				final Waypoint waypoint = waypoints[i];
				
				if(waypoint.runId != runId) {
					continue;
				}
				
				if(waypoint.ground) {
					batch.setColor(0f, 1f, 0f, 0.25f);
				}
				else {
					batch.setColor(1f, 1f, 0f, 0.25f);
				}
				
				batch.draw(
						game.assets.block(), 
						(clipX + waypoint.x) * tileWidth + tileWidth / 4f - viewX, 
						(clipY + waypoint.y) * tileHeight + tileHeight / 4f - viewY, 
						tileWidth / 2f, 
						tileHeight / 2f);
			}

			for(int i = 0; i < waypoints.length; i += 1) {
				final Waypoint waypoint = waypoints[i];
				
				if(waypoint.runId != runId) {
					continue;
				}
				
				for(int ii = 0; ii < waypoint.connections.size; ii += 1) {
					final Waypoint.ConnectionType connectionType = waypoint.connectionTypes.get(ii);
					
					switch(connectionType) {
						case Walk:
							batch.setColor(1f, 1f, 1f, 1f);
							break;
						case Fall:
							batch.setColor(1f, 0f, 0f, 1f);
							break;
						case Jump:
							batch.setColor(0f, 0f, 1f, 1f);
							break;
					}

					final Waypoint connection = waypoint.connections.get(ii);
					
					final float x1 = (clipX + waypoint.x) * tileWidth + tileWidth / 2f - viewX;
					final float y1 = (clipY + waypoint.y) * tileHeight + tileHeight / 2f - viewY;
					final float x2 = (clipX + connection.x) * tileWidth + tileWidth / 2f - viewX;
					final float y2 = (clipY + connection.y) * tileHeight + tileHeight / 2f - viewY;
					
					final float rotation = tmpVector.set(x2 - x1, y2 - y1).angle() - 90f;

					batch.draw(
							game.assets.block(), 
							x1 + 0.5f, 
							y1, 
							0.5f, 
							0f, 
							1f, 
							tmpVector.len(), 
							1f, 
							1f, 
							rotation);
					
					batch.draw(
							game.assets.block(), 
							x2 + 0.5f, 
							y2, 
							0.5f, 
							0f, 
							1f, 
							tileWidth * 0.2f, 
							1f, 
							1f, 
							rotation - 220f);
					
					batch.draw(
							game.assets.block(), 
							x2 + 0.5f, 
							y2, 
							0.5f, 
							0f, 
							1f, 
							tileWidth * 0.2f, 
							1f, 
							1f, 
							rotation - 140f);
				}
			}
			
			batch.setColor(1f, 1f, 1f, 1f);
		}
		
		// ******************************
		// RENDER PAWNS
		// ******************************
		{
			final TextureRegion lassoPattern = game.assets.ropePatternTest();
			final TextureRegion lassoEnd = game.assets.ropePatternEndTest();
			final float lassoPatternWidth = lassoPattern.getRegionWidth();
			final float lassoPatternHeight = lassoPattern.getRegionHeight();
			final float lassoEndWidth = lassoEnd.getRegionWidth();
			final float lassoEndHeight = lassoEnd.getRegionHeight();
			final float scaledLassoPatternWidth = lassoPatternWidth * assetScale;
			final float scaledLassoPatternHeight = lassoPatternHeight * assetScale;
			final float scaledLassoEndWidth = lassoEndWidth * assetScale;
			final float scaledLassoEndHeight = lassoEndHeight * assetScale;
			
			final Array<Pawn> pawns = map.pawns;
			final int n = pawns.size;
			
			for(int i = 0; i < n; i += 1) {
				final Pawn pawn = pawns.get(i);
				final Skeleton skeleton = pawn.skeleton();
				
				final float pawnX = pawn.x;
				final float pawnY = pawn.y;

				if(cullX < pawnX + pawn.width() && 
				   cullY < pawnY + pawn.height() &&
				   cullRight > pawnX &&
				   cullTop > pawnY) {
					
					// ******************************
					// RENDER LASSO
					// ******************************
					final Lasso lasso = pawn.lasso;
					if(lasso.enabled) {
						final float length = lasso.length;
						final float divPattern = length / scaledLassoPatternWidth;
						final int fullFitPattern = (int)(divPattern);
						final float leftOver = scaledLassoPatternWidth * (divPattern - (int)divPattern);
						final float startX = lasso.startX - viewX;
						final float startY = lasso.startY - viewY;
						final float endX = lasso.endX - viewX;
						final float endY = lasso.endY - viewY;
						final float angle = lasso.angle;
						
						for(int ii = 0; ii < fullFitPattern; ii += 1) {
							final float percent = (scaledLassoPatternWidth * ii) / length;
							
							batch.draw(
									lassoPattern,
									startX + (endX - startX) * percent,
									startY + (endY - startY) * percent,
									0f,
									scaledLassoPatternHeight / 2f,
									scaledLassoPatternWidth,
									scaledLassoPatternHeight,
									1f,
									1f,
									angle);
						}
						
						if(leftOver > 0f) {
							final int regionWidth = lassoPattern.getRegionWidth();
							
							final float percentScale = leftOver / scaledLassoPatternWidth;
							lassoPattern.setRegionWidth((int)(regionWidth * percentScale));
							
							final float percent = (scaledLassoPatternWidth * fullFitPattern) / length;
							batch.draw(
									lassoPattern,
									startX + (endX - startX) * percent,
									startY + (endY - startY) * percent,
									0f,
									scaledLassoPatternHeight / 2f,
									scaledLassoPatternWidth * percentScale,
									scaledLassoPatternHeight,
									1f,
									1f,
									angle);
							
							lassoPattern.setRegionWidth(regionWidth);
						}
						
						batch.draw(
								lassoEnd,
								endX - scaledLassoEndWidth / 2f,
								endY - scaledLassoEndHeight / 2f,
								scaledLassoEndWidth / 2f,
								scaledLassoEndHeight / 2f,
								scaledLassoEndWidth,
								scaledLassoEndHeight,
								1f,
								1f,
								angle);
					}
					
					skeleton.setPosition(
							pawnX + pawn.xDiff() - viewX, 
							pawnY + pawn.yDiff() - viewY);

					batch.setColor(1f, 1f, 1f, 0.25f);
					batch.draw(
							game.assets.block(),
							pawnX - viewX,
							pawnY - viewY,
							pawn.width(),
							pawn.height());
					batch.setColor(1f, 1f, 1f, 1f);
					
					skeletonRenderer.draw(batch, skeleton);
					
					final float gravity = tileHeight * 10f;
					final float maxPawnVelocityY = pawn.jumpHeightPerc * tileHeight;
					final float jumpHeight = JumpUtil.jumpHeight(maxPawnVelocityY, -gravity);

					if(!pawn.isInAir) {
						batch.setColor(1f, 1f, 1f, 0.25f);
						batch.draw(
								game.assets.block(),
								pawnX - viewX,
								pawnY - viewY + jumpHeight,
								pawn.width(),
								pawn.height());
						batch.setColor(1f, 1f, 1f, 1f);
					}
				}
			}	
		}
		
		// ******************************
		// RENDER TAKABLES
		// ******************************
		{
			final Array<Takable> takables = map.takables;
			int n = takables.size;
			
			for(int i = 0; i < n; i++) {
				final Takable takable = takables.get(i);
				
				if(takable instanceof RegionTakable) {
					batch.draw(((RegionTakable) takable).region(), 
								takable.x(), 
								takable.y());
				}
				else if(takable instanceof SkeletonTakable) {
					final SkeletonTakable t = ((SkeletonTakable) takable);
					
					t.skeleton().setPosition(t.x() + t.xDiff(), t.y() + t.yDiff());
					skeletonRenderer.draw(batch, t.skeleton());
				}
			}
		}

		// *********************************************************
		// RENDER LAYERS AFTER MAIN, PAWN, TAKABLES
		// *********************************************************
		{
			batch.setColor(1f, 1f, 1f, 1f);
			
			final int n = layers.length;
			
			for(int i = map.mainLayerIndex + 1; i < n; i += 1) {
				final Layer layer = layers[i];
				final Tiles tiles = layer.tiles;
				
				if(tiles != null) {
					final int xCoord = tiles.tileXCoord();
					final int yCoord = tiles.tileYCoord();
					
					int drawLeft = xCoord;
					int drawBottom = yCoord;
					int drawRight = drawLeft + tiles.width - 1;
					int drawTop = drawBottom + tiles.height - 1;

					if(drawLeft <= cullTileRight &&
					   drawBottom <= cullTileTop &&
					   drawRight >= cullTileLeft &&
					   drawTop >= cullTileBottom) {
						
						if(drawLeft < cullTileLeft) {
							drawLeft = cullTileLeft;
						}
						
						if(drawBottom < cullTileBottom) {
							drawBottom = cullTileBottom;
						}
						
						if(drawRight > cullTileRight) {
							drawRight = cullTileRight;
						}
						
						if(drawTop > cullTileTop) {
							drawTop = cullTileTop;
						}

						drawLeft -= xCoord;
						drawBottom -= yCoord;
						drawRight -= xCoord;
						drawTop -= yCoord;
						
						final float x = tileWidth * xCoord - viewX;
						final float y = tileHeight * yCoord - viewY;
						
						renderTiles(tiles, x, y, tileWidth, tileHeight, drawLeft, drawBottom, drawRight, drawTop);
					}
				}
				
				final StaticDecoration[] decorations = layer.decorations();
				if(decorations != null) {
					renderDecorations(decorations, 
									  viewX, 
									  viewY, 
									  cullX, 
									  cullY, 
									  cullRight, 
									  cullTop);
				}
			}
		}
	}
	
	private void renderDecorations(final StaticDecoration[] decorations, 
								   final float x, 
								   final float y,
								   final float cullX,
								   final float cullY,
								   final float cullRight,
								   final float cullTop) {
		
		final int n = decorations.length;
		
		for(int i = 0; i < n; i += 1) {
			final StaticDecoration decoration = decorations[i];
			
			final float decorationX = decoration.x();
			final float decorationY = decoration.y();
			final float decorationWidth = decoration.width();
			final float decorationHeight = decoration.height();
			
			if(decorationX > cullRight ||
			   decorationY > cullTop ||
			   decorationX + decorationWidth < cullX ||
			   decorationY + decorationHeight < cullY) {
				
			} else {
				batch.draw(decoration.region(), decorationX - x, decorationY - y, decorationWidth, decorationHeight);
			}
		}
	}
	
	private void renderTiles(final Tiles tiles, 
							 final float x, 
							 final float y, 
						   	 final float tileWidth, 
							 final float tileHeight, 
							 final int startX, 
							 final int startY, 
							 final int endX, 
							 final int endY) {
		
		final int width = tiles.width;
		final Tile[] tileset = tiles.tileset;
		final byte[] tileIndexes = tiles.tiles;
		
		for(int iy = startY; iy <= endY; iy += 1) {
			for(int ix = startX; ix <= endX; ix += 1) {
				final byte tileIndex = tileIndexes[iy * width + ix];
				
				if(tileIndex > -128) {
					final Tile tile = tileset[tileIndex + 127];
					if(!tile.decoration) {
						batch.draw(tile.region(), x + ix * tileWidth, y + iy * tileHeight, tileWidth, tileHeight);
					} else {
						batch.draw(tile.region(), x + ix * tileWidth, y + iy * tileHeight, tile.width(), tile.height());
					}
				}
			}
		}
	}
	
}
