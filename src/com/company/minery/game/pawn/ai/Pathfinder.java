package com.company.minery.game.pawn.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.IntArray;
import com.company.minery.game.map.Map;
import com.company.minery.game.map.Tile;
import com.company.minery.game.map.Tiles;
import com.company.minery.game.pawn.Pawn;
import com.company.minery.game.pawn.Pawn.MovementDirection;
import com.company.minery.utils.JumpUtil;

public final class Pathfinder {
	
	public static final class PathTile {
		public int x;
		public int y;
		public boolean jump;
		public Pawn.MovementDirection direction;
	}
	
	private static final class PathNode extends BinaryHeap.Node {
		private final int x;
		private final int y;
		private Pawn.MovementDirection direction;
		
		private int runId;
		private int closedId;
		private int pathCost;
		private PathNode parent;
		private boolean inAir;

		public PathNode(final float value,
						final int x, 
						final int y) {
			
			super(value);
			
			this.x = x;
			this.y = y;
		}
	}
	
	private final int width;
	private final int height;
	private final PathNode[] nodes;
	private final BinaryHeap<PathNode> open;
	
	private int runId;
	
	// **************************************
	// REPETITIVE ARGUMENT REDUCTION
	// **************************************
	private Pawn pawn;
	private PathNode rootNode;
	private float tileWidth;
	private float tileHeight;
	private int targetX;
	private int targetY;
	private Tiles mainLayerTiles;
	private int pathfinderX;
	private int pathfinderY;
	private int mainLayerWidth;
	private int mainLayerHeight;
	private int startX;
	private int startY;
	private int pawnWidth;
	private int pawnHeight;
	private PhysicsInfo physicsInfo;
	
	public Pathfinder(final int width, 
					  final int height) {
		
		this.width = width;
		this.height = height;
		this.open = new BinaryHeap<PathNode>(width * 4, false);
		this.nodes = new PathNode[width * height];
	}
	
	public boolean findPath(final Map map,
							final Pawn pawn,
							final PhysicsInfo physicsInfo,
							final int startX,
							final int startY,
							final int targetX, 
							final int targetY) {
		
		final IntArray pathOut = pawn.path;
		final PathTile currentPathTileOut = pawn.currentPathTile;

		currentPathTileOut.jump = false;
		currentPathTileOut.x = 0;
		currentPathTileOut.y = 0;
		currentPathTileOut.direction = Pawn.MovementDirection.Idle;
		
		pathOut.clear();
		open.clear();
		
		runId += 1;
		if(runId < 0) {
			runId = 1;
		}

		// Make local references of fields.
		final BinaryHeap<PathNode> open = this.open;
		final PathNode[] nodes = this.nodes;
		final int width = this.width;
		final int height = this.height;
		
		// If distance to the target point is greater than path finder's width or height, the path cannot be found.
		if(Math.abs(targetX - startX) > width || 
		   Math.abs(targetY - startY) > height) {
			
			return false;
		}
		
		final Tiles mainLayerTiles = map.mainLayer.tiles;
		
		final int mainLayerWidth = mainLayerTiles.width;
		final int mainLayerHeight = mainLayerTiles.height;
		
		// Calculate the position of path finder.
		final int pathfinderX = MathUtils.clamp(startX - width / 2, 0, mainLayerWidth - width);
		final int pathfinderY = MathUtils.clamp(startY - height / 2, 0, mainLayerHeight - height);
		
		// Translate startX, startY, targetX and targetY coordinates to local.
		final int localStartX = startX - pathfinderX;
		final int localStartY = startY - pathfinderY;
		final int localTargetX = targetX - pathfinderX;
		final int localTargetY = targetY - pathfinderY;

		final int pawnWidth;
		final int pawnHeight;
		
		{
			final float tileWidth = map.tileWidth();
			final float tileHeight = map.tileHeight();
			final float pawnFloatWidth = pawn.width();
			final float pawnFloatHeight = pawn.height();
			
			pawnWidth = (int)(pawnFloatWidth / tileWidth) + (pawnFloatWidth % tileWidth == 0 ? 0 : 1);
			pawnHeight = (int)(pawnFloatHeight / tileHeight) + (pawnFloatHeight % tileHeight == 0 ? 0 : 1);
		}
		
		final PathNode rootNode;
		
		// Create/fetch root node at the startX, startY position.
		{
			final int index = localStartY * width + localStartX;
			final PathNode tmpRoot = nodes[index];
			
			if(tmpRoot == null) {
				rootNode = new PathNode(0, localStartX, localStartY);
				nodes[index] = rootNode;
			}
			else {
				rootNode = tmpRoot;
			}
		}
		
		// Setup root node for this run.
		rootNode.parent = null;
		rootNode.pathCost = 0;
		rootNode.direction = MovementDirection.Idle;
		open.add(rootNode, 0);
		
		// Set parameter reduction values
		this.pawn = pawn;
		this.physicsInfo = physicsInfo;
		this.tileWidth = map.tileWidth();
		this.tileHeight = map.tileHeight();
		this.mainLayerTiles = mainLayerTiles;
		this.mainLayerWidth = mainLayerWidth;
		this.mainLayerHeight = mainLayerHeight;
		this.pathfinderX = pathfinderX;
		this.pathfinderY = pathfinderY;
		this.startX = localStartX;
		this.startY = localStartY;
		this.targetX = localTargetX;
		this.targetY = localTargetY;
		this.pawnWidth = pawnWidth;
		this.pawnHeight = pawnHeight;
		this.rootNode = rootNode;
		
		if(isInAir(localTargetX, localTargetY)) {
			System.out.println("Pathfinder found that position is in air, exiting");
			return false;
		}
		
		rootNode.inAir = isInAir(localStartX, localStartY);
		
		// Cache last indexes of width and height.
		final int lastColumn = width - 1;
		final int lastRow = height - 1;
		
		final int baseCost = 10;
		final int baseDiagonalCost = baseCost + 4;
		
		while(open.size > 0) {
			final PathNode node = open.pop();
			
			// If reached end
			if(node.x == localTargetX && 
			   node.y == localTargetY) {
				
				PathNode target = node;
				
				// If target is root no path is needed, because the pawn is already at the target position.
				if(target == rootNode) {
					currentPathTileOut.x = pathfinderX + rootNode.x;
					currentPathTileOut.y = pathfinderY + rootNode.y;
					return false;
				}
				
				while(target != rootNode) {
					final PathNode parent = target.parent;
					
					if(parent == rootNode) {
						currentPathTileOut.x = pathfinderX + rootNode.x;
						currentPathTileOut.y = pathfinderY + rootNode.y;
						currentPathTileOut.direction = target.direction;
					}
					
					target = target.parent;
				}
				
				return true;
			}
			
			node.closedId = runId;
			
			final int x = node.x;
			final int y = node.y;
			
			if(x < lastColumn) {
				addNode(node, x, y, 1, 0, baseCost);
				
				/*if(y < lastRow) {
					addNode(node, x, y, 1, 1, baseDiagonalCost); 
				}*/
				
				if(y > 0) {
					addNode(node, x, y, 1, -1, baseDiagonalCost);
					//addNode(node, x, y, 1, 0, baseCost);
					//addNode(node, x, y, 0, -1, baseCost);
				}
			}
			
			if(x > 0) {
				addNode(node, x, y, -1, 0, baseCost);
				
				/*if(y < lastRow) {
					addNode(node, x, y, -1, 1, baseDiagonalCost);
				}*/
				
				if(y > 0) {
					addNode(node, x, y, -1, -1, baseDiagonalCost);
					//addNode(node, x, y, -1, 0, baseCost);
					//addNode(node, x, y, 0, -1, baseCost);
				}
			}
			 
			/*if(y < lastRow) {
				addNode(node, x, y, 0, 1, baseCost);
			}*/
			
			if(y > 0) {
				addNode(node, x, y, 0, -1, baseCost);
			}
		}
		
		return false;
	}
	
	private boolean isInAir(final int x, 
							final int y) {
		
		final Tiles mainLayerTiles = this.mainLayerTiles;
		final int mainLayerWidth = this.mainLayerWidth;
		final byte[] byteTiles = mainLayerTiles.tiles;
		final int globalX = pathfinderX + x;
		final int globalY = pathfinderY + y;
		
		if(globalY == 0) {
			return false;
		}
		
		final int iy = globalY - 1;
		final int nx = globalX + pawnWidth;
		for(int ix = globalX; ix < nx; ix += 1) {
			final int tileValue = byteTiles[iy * mainLayerWidth + ix] + 127;
			
			if(tileValue != -1) {
				return false;
			}
		}
		
		return true;
	}

	private void addNode(final PathNode parent, 
						 final int prevX, 
						 final int prevY, 
						 final int xDirection,
						 final int yDirection,
						 final int cost) {
		
		// Make local references.
		final int pathfinderX = this.pathfinderX;
		final int pathfinderY = this.pathfinderY;
		final int pawnWidth = this.pawnWidth;
		final int pawnHeight = this.pawnHeight;
		final Tiles mainLayerTiles = this.mainLayerTiles;
		final int mainLayerWidth = this.mainLayerWidth;
		final int mainLayerHeight = this.mainLayerHeight;
		final int targetX = this.targetX;
		final int targetY = this.targetY;
		
		// Calculate current position in global and local coords.
		final int x = prevX + xDirection;
		final int y = prevY + yDirection;
		final int globalX = pathfinderX + x;
		final int globalY = pathfinderY + y;
		
		final int costMod;
		final boolean nodeInAir = isInAir(x, y);
		
		// If globaly the pawn would be ouside map, this is not a valid position
		if(globalX > mainLayerWidth - pawnWidth ||
		   globalY > mainLayerHeight - pawnHeight) {
			
			costMod = -1;
		}
		else {
			int mod = 0;
			
			final byte[] byteTiles = mainLayerTiles.tiles;
			final int nx = globalX + pawnWidth;
			final int ny = globalY + pawnHeight;
			
			tilesLoop:
			for(int iy = globalY; iy < ny; iy += 1) {
				for(int ix = globalX; ix < nx; ix += 1) {
					if(byteTiles[iy * mainLayerWidth + ix] != -128) {
						mod = -1;
						break tilesLoop;
					}
				}
			}
			
			if(mod != -1) {
				if(nodeInAir && xDirection != 0) {
					int sameYInARow = 1;
					boolean countingSameYInARow = true;
					PathNode firstSameY = parent;
					int airPathRootX = x;
					int airPathRootY = y;
					
					PathNode currentNode = parent;
					
					while(true) {
						if(countingSameYInARow) {
							if(currentNode.y == y) {
								sameYInARow += 1;
								firstSameY = currentNode;
							}
							else {
								countingSameYInARow = false;
							}
						}
						
						if(!currentNode.inAir) {
							break;
						}
						
						airPathRootX = currentNode.x;
						airPathRootY = currentNode.y;
						
						if(currentNode == rootNode) {
							break;
						}
						
						currentNode = currentNode.parent;
					}
					
					//System.out.println("sameYInARow: " + sameYInARow);
					//System.out.println("startIsRoot: " + startIsRoot);
					
					float xDistancePerTileHeight = JumpUtil.fallDurationWithSpeedLimit(
							0, 
							physicsInfo.maxVelocityY, 
							0f, 
							tileHeight, 
							physicsInfo.gravity);// * physicsInfo.maxVelocityX;
					
					System.out.println("fall duration per tileHeight: " + xDistancePerTileHeight);
					xDistancePerTileHeight *= physicsInfo.maxVelocityX;
					
					//System.out.println("xDistancePerTileHeight: " + xDistancePerTileHeight);
					
					final float additionalHeight = pawn.y - (rootNode.y + pathfinderY) * tileHeight;
					final float additionalDistance = additionalHeight / tileHeight * xDistancePerTileHeight;
					
					final float finalDistance = xDistancePerTileHeight + additionalDistance;					
					int maxSameYInARow = (int)(finalDistance / tileWidth) + (finalDistance % tileWidth == 0 ? 0 : 1);
					
					if(!firstSameY.inAir && (int)additionalHeight == 0) {
						maxSameYInARow -= 1;
					}
					
					//System.out.println("additionalHeight: " + additionalHeight);
					//System.out.println("additionalDistance: " + additionalDistance);
					//System.out.println("finalDistance: " + finalDistance);
					//System.out.println("maxSameYInARow: " + maxSameYInARow);
					
					if(maxSameYInARow < sameYInARow) {
						mod = -1;
					}
					else {
						final float displacementX = Math.abs(x - airPathRootX) * tileWidth;
						final float displacementY = Math.abs(y - airPathRootY) * tileHeight + additionalHeight;

						System.out.println("DisplacementX: " + displacementX);
						System.out.println("DisplacementY: " + displacementY);
						System.out.println("xDistancePerTileHeight: " + xDistancePerTileHeight);
						System.out.println("Tiles in displacement: " + (displacementY / tileHeight));
						System.out.println("Tiles height: " + tileHeight);
						System.out.println("X movement limit: " + ((displacementY / tileHeight) * xDistancePerTileHeight));
						
						if(displacementX > (displacementY / tileHeight) * xDistancePerTileHeight) {
							mod = -1;
						}
					}
				}
			}
			
			costMod = mod;
		}
		
		if(costMod < 0) {
			return;
		}
		
		final int pathCost = parent.pathCost + cost + costMod;
		final float score = pathCost + Math.abs(x - targetX) + Math.abs(y - targetY);

		final int index = y * width + x;
		final PathNode node = nodes[index];
		
		if(node != null && node.runId == runId) { // Node already encountered for this run.
			if(node.closedId != runId && pathCost < node.pathCost) { // Node isn't closed and new cost is lower.
				// Update the existing node.
				open.setValue(node, score);
				node.parent = parent;
				node.pathCost = pathCost;
				node.direction = MovementDirection.Idle;
				node.inAir = nodeInAir;
				
				switch(xDirection) {
					case 0:
						node.direction = MovementDirection.Idle;
						break;
					case 1:
						node.direction = MovementDirection.Right;
						break;
					case -1:
						node.direction = MovementDirection.Left;
						break;
				}
			}
		} 
		else {
			final PathNode cNode;
			
			// Use node from the cache or create a new one.
			if(node == null) {
				cNode = new PathNode(0, x, y);
				nodes[index] = cNode;
			}
			else {
				cNode = node;
			}
			
			open.add(cNode, score);
			
			cNode.runId = runId;
			cNode.parent = parent;
			cNode.pathCost = pathCost;
			cNode.direction = MovementDirection.Idle;
			cNode.inAir = nodeInAir;

			switch(xDirection) {
				case 0:
					cNode.direction = MovementDirection.Idle;
					break;
				case 1:
					cNode.direction = MovementDirection.Right;
					break;
				case -1:
					cNode.direction = MovementDirection.Left;
					break;
			}
		}
	}
	
}
