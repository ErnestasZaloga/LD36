package com.company.minery.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.company.minery.game.map.Layer;
import com.company.minery.game.map.Map;
import com.company.minery.game.map.StaticDecoration;
import com.company.minery.game.map.Tile;
import com.company.minery.game.map.Tiles;
import com.company.minery.game.player.Player;

public class GameRender {

	private final PolygonSpriteBatch batch;
	
	public GameRender(final PolygonSpriteBatch batch) {
		this.batch = batch;
	}
	
	public void render(final Game game) {
		batch.setColor(1f, 1f, 1f, 1f);
		
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
		
		// ******************************
		// RENDER PLAYERS
		// ******************************
		{
			final Array<Player> players = game.players;
			final int n = players.size;
			
			for(int i = 0; i < n; i += 1) {
				final Player player = players.get(i);
				
				final float pawnX = player.x;
				final float pawnY = player.y;

				if(cullX < pawnX + player.width && cullY < pawnY + player.height && cullRight > pawnX && cullTop > pawnY) {
					batch.setColor(1f, 1f, 1f, 1f);
					batch.draw(player.body.texture, pawnX - viewX + player.body.offsetX, pawnY - viewY + player.body.offsetY, player.body.texture.getWidth(), player.body.texture.getHeight());
					batch.draw(player.head.texture, pawnX - viewX + player.head.offsetX, pawnY - viewY + player.head.offsetY, player.head.texture.getWidth(), player.head.texture.getHeight());
					batch.draw(player.leftHand.texture, pawnX - viewX + player.leftHand.offsetX, pawnY - viewY + player.leftHand.offsetY, player.leftHand.texture.getWidth(), player.leftHand.texture.getHeight());
					batch.draw(player.rightHand.texture, pawnX - viewX + player.rightHand.offsetX, pawnY - viewY + player.rightHand.offsetY, player.rightHand.texture.getWidth(), player.rightHand.texture.getHeight());
					batch.draw(player.leftFoot.texture, pawnX - viewX + player.leftFoot.offsetX, pawnY - viewY + player.leftFoot.offsetY, player.leftFoot.texture.getWidth(), player.leftFoot.texture.getHeight());
					batch.draw(player.rightFoot.texture, pawnX - viewX + player.rightFoot.offsetX, pawnY - viewY + player.rightFoot.offsetY, player.rightFoot.texture.getWidth(), player.rightFoot.texture.getHeight());
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
					
					if(tile.decoration) {
						batch.draw(tile.region, x + ix * tileWidth, y + iy * tileHeight, tile.width, tile.height);
					}
					else {
						batch.draw(tile.region, (int)(x + ix * tileWidth), (int)(y + iy * tileHeight), tileWidth, tileHeight);
					}
				}
			}
		}
	}
	
}
