package com.company.minery.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.company.minery.Constants;
import com.company.minery.game.GameAssets.TextureRegionExt;
import com.company.minery.game.map.Layer;
import com.company.minery.game.map.Map;
import com.company.minery.game.map.StaticDecoration;
import com.company.minery.game.map.Tile;
import com.company.minery.game.map.Tiles;
import com.company.minery.game.player.Player;
import com.company.minery.game.player.Player.MovementDirection;
import com.company.minery.game.player.Spear;

public class GameRender {

	private static final Vector2 tmpVector = new Vector2();
	
	private final PolygonSpriteBatch batch;
	
	public GameRender(final PolygonSpriteBatch batch) {
		this.batch = batch;
	}
	
	public void render(final Game game) {
		batch.setColor(1f, 1f, 1f, 1f);
		
		final Vector2 tmpVector = new Vector2();
		
		final Map map = game.currentMap();
		final float viewX = map.viewX;
		final float viewY = map.viewY;
		
		final float tileWidth = map.tileWidth;
		final float tileHeight = map.tileHeight;
		
		final float screenWidth = Gdx.graphics.getWidth();
		final float screenHeight = Gdx.graphics.getHeight();
		
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
				final int xCoord = tiles.tileXCoord;
				final int yCoord = tiles.tileYCoord;
				
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
			
			final StaticDecoration[] decorations = layer.decorations;
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
		// RENDER PLAYERS
		// ******************************
		{
			final Array<Player> players = game.players;
			final int n = players.size;
			
			for(int i = 0; i < n; i += 1) {
				final Player player = players.get(i);
				
				batch.setColor(1f, 1f, 1f, 1f);

				if(player.flip) {
					player.body.texture.flip(true, false);
					player.head.texture.flip(true, false);
					player.rightHand.texture.flip(true, false);
					player.leftFoot.texture.flip(true, false);
				}
				
				final float pawnX = player.x - viewX;
				final float pawnY = player.y - viewY;
				
				batch.draw(
						player.leftFoot.texture,
						(int)(pawnX + player.leftFoot.offsetX),
						(int)(pawnY + player.leftFoot.offsetY),
						(int)(player.leftFoot.originX),
						(int)(player.leftFoot.originY),
						(int)(player.leftFoot.texture.getWidth()),
						(int)(player.leftFoot.texture.getHeight()),
						1,
						1,
						player.leftFoot.rotation);
				
				batch.draw(
						player.rightFoot.texture,
						(int)(pawnX + player.rightFoot.offsetX),
						(int)(pawnY + player.rightFoot.offsetY),
						(int)(player.rightFoot.originX),
						(int)(player.rightFoot.originY),
						(int)(player.rightFoot.texture.getWidth()),
						(int)(player.rightFoot.texture.getHeight()),
						1,
						1,
						player.rightFoot.rotation);
				
				batch.draw(
						player.body.texture,
						(int)(pawnX + player.body.offsetX),
						(int)(pawnY + player.body.offsetY),
						(int)(player.body.originX),
						(int)(player.body.originY),
						(int)(player.body.texture.getWidth()),
						(int)(player.body.texture.getHeight()),
						1,
						1,
						player.body.rotation);
				
				batch.draw(
						player.head.texture,
						(int)(pawnX + player.head.offsetX),
						(int)(pawnY + player.head.offsetY),
						(int)(player.head.originX),
						(int)(player.head.originY),
						(int)(player.head.texture.getWidth()),
						(int)(player.head.texture.getHeight()),
						1,
						1,
						player.head.rotation);
				
				batch.draw(
						player.leftHand.texture,
						(int)(pawnX + player.leftHand.offsetX),
						(int)(pawnY + player.leftHand.offsetY),
						(int)(player.leftHand.originX),
						(int)(player.leftHand.originY),
						(int)(player.leftHand.texture.getWidth()),
						(int)(player.leftHand.texture.getHeight()),
						1,
						1,
						player.leftHand.rotation);
				
				final float rightHandX = pawnX + player.rightHand.offsetX;
				final float rightHandY = pawnY + player.rightHand.offsetY;
				
				if(player.hasWeapon) {
					final TextureRegionExt spearTexture = game.assets.spear;
					
					if(player.flip) {
						spearTexture.flip(true, false);
					}
					
					final float spearAlignX = rightHandX + player.rightHand.originX;
					final float spearAlignY = rightHandY + player.rightHand.originY;
					
					final float spearX = spearAlignX - spearTexture.getWidth() / 2;
					final float spearY = spearAlignY - spearTexture.getHeight() / 2;
					
					batch.draw(
							spearTexture,
							(int)(spearX),
							(int)(spearY),
							(int)(spearTexture.getWidth() / 2),
							(int)(spearTexture.getHeight() / 2),
							(int)(spearTexture.getWidth()),
							(int)(spearTexture.getHeight()), 
							1, 1,
							player.rightHand.rotation);
					
					if(player.flip) {
						spearTexture.flip(true, false);
					}
				}
				
				batch.draw(
						player.rightHand.texture,
						(int)(rightHandX),
						(int)(rightHandY),
						(int)(player.rightHand.originX),
						(int)(player.rightHand.originY),
						(int)(player.rightHand.texture.getWidth()),
						(int)(player.rightHand.texture.getHeight()),
						1,
						1,
						player.rightHand.rotation);
				
				if(player.flip) {
					player.body.texture.flip(true, false);
					player.head.texture.flip(true, false);
					player.rightHand.texture.flip(true, false);
					player.leftFoot.texture.flip(true, false);
				}
			}
		}
		
		// ******************************
		// RENDER SPEARS
		// ******************************
		{
			final Array<Spear> spears = game.spears;
			final int n = spears.size;
			
			for(int i = 0; i < n; i += 1) {
				final Spear spear = spears.get(i);
				
				if(spear.movementDirection == MovementDirection.Idle) {
					renderSpear(spear, viewX, viewY);
				}
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
			
			final StaticDecoration[] decorations = mainLayer.decorations;
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
		
		batch.setColor(1f, 1f, 1f, 1f);
				
		
		// ******************************
		// RENDER SPEARS
		// ******************************
		{
			final Array<Spear> spears = game.spears;
			final int n = spears.size;
			
			for(int i = 0; i < n; i += 1) {
				final Spear spear = spears.get(i);
				
				if(spear.movementDirection != MovementDirection.Idle) {
					tmpVector.x = spear.velocityX;
					tmpVector.y = spear.velocityY;
					final float rotation = tmpVector.angle();
					spear.lastRotation = rotation;
					renderSpear(spear, viewX, viewY);
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
					final int xCoord = tiles.tileXCoord;
					final int yCoord = tiles.tileYCoord;
					
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
				
				final StaticDecoration[] decorations = layer.decorations;
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
		
		// *********************************************************
		// RENDER LOCAL UI
		// *********************************************************
		
		final Player localPlayer = game.localPlayer();
		
		if(localPlayer.dead) {
			batch.setColor(1f, 0f, 0f, 1f);
		}
		
		batch.draw(
				game.assets.arrow,
				(int)(localPlayer.x - viewX + localPlayer.width / 2f - game.assets.arrow.getWidth() / 2f),
				(int)(localPlayer.y - viewY + localPlayer.height + game.assets.arrow.getHeight() * 2f),
				(int)game.assets.arrow.getWidth(),
				(int)game.assets.arrow.getHeight());
		
		if(localPlayer.dead) {
			batch.setColor(1f, 1f, 1f, 1f);
		}
		
		if(game.message != null) {
			batch.draw(game.message, Gdx.graphics.getWidth() / 2f - game.message.getWidth() / 2f, Gdx.graphics.getHeight() / 2f - game.message.getHeight() / 2f, game.message.getWidth(), game.message.getHeight());
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
	
	private void renderSpear(final Spear spear,
							 final float viewX,
							 final float viewY) {
		
		final float regionHeight = spear.region.getHeight();
		
		final float x = spear.x;
		final float y = spear.y;
		
		final float handleWidth = spear.region.getWidth() - spear.region.getWidth() * Constants.SPEAR_TIP_MOD;
		final float handleHeight = spear.region.getHeight() * Constants.SPEAR_HANDLE_MOD;
		
		batch.draw(spear.region, (int)(x - viewX - handleWidth), (int)(y - viewY), (int)(handleWidth), (int)(regionHeight * Constants.SPEAR_HANDLE_MOD / 2 + handleHeight / 2f), (int) spear.region.getWidth(), (int) regionHeight, 1, 1, spear.lastRotation);
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
					
					if(tile.decoration) {
						batch.draw(tile.region, x + ix * tileWidth, y + iy * tileHeight, tile.width, tile.height);
					}
					else {
						batch.draw(tile.region, (int)(x + ix * tileWidth), (int)(y + iy * tileHeight), (int) tileWidth, (int) tileHeight);
					}
				}
			}
		}
	}
	
}
