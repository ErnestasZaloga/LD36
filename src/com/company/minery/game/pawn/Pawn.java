package com.company.minery.game.pawn;

import com.badlogic.gdx.utils.IntArray;
import com.company.minery.game.pawn.ai.Pathfinder;
import com.company.minery.utils.SkeletonBounds;
import com.company.minery.utils.spine.Animation;
import com.company.minery.utils.spine.Skeleton;
import com.company.minery.utils.spine.SkeletonData;

abstract public class Pawn {
	
	public static enum MovementDirection {
		Idle(0, 0),
		Left(1, -1), 
		Right(2, 1);

		public static MovementDirection valueOf(final int value) {
			switch(value) {
				case 0:
					return Idle;
				case 1:
					return Left;
				case 2:
					return Right;
			}
			throw new IllegalArgumentException("invalid value");
		}
		
		public final int id;
		public final int mul;
		
		private MovementDirection(final int id,
								  final int mul) {

			this.id = id;
			this.mul = mul;
		}
	}
	
	private static long uidCounter = Long.MIN_VALUE;
	private static final float[] tmpSkeletonBounds = new float[4];
	
	public final Lasso lasso = new Lasso();
	public final long uid;
	
	public final Pathfinder.PathTile currentPathTile = new Pathfinder.PathTile();
	public boolean initialPathFound;
	public boolean pathfindingEnabled;
	public float pathfindingMapX;
	public float pathfindingMapY;
	public final IntArray path = new IntArray();
	
	public MovementDirection movementDirection = MovementDirection.Idle;
	public float velocityX;
	public float velocityY;
	
	public float jumpHeightPerc = 5f;
	public float runSpeedPerc = 6f;
	
	public boolean requestsJump;
	public boolean isInAir;
	public boolean isJumping;
	public boolean isRunning;
	
	public boolean requestsLasso;
	public float lassoTargetX;
	public float lassoTargetY;
	
	public boolean requestsMining;
	public float miningTargetX;
	public float miningTargetY;
	
	private float xDiff; /**/ public final float xDiff() { return xDiff; }
	private float yDiff; /**/ public final float yDiff() { return yDiff; }
	public float x;
	public float y;
	private float width; /**/ public final float width() { return width; }
	private float height; /**/ public final float height() { return height; }
	
	public float animationTimer;
	
	private Skeleton skeleton; /**/ public final Skeleton skeleton() { return skeleton; }
	
	//Main animations
	private Animation idleAnimation; /**/ public final Animation idleAnimation() { return idleAnimation; }
	private Animation jumpAnimation; /**/ public final Animation jumpAnimation() { return jumpAnimation; }
	private Animation runAnimation; /**/ public final Animation runAnimation() { return runAnimation; }
	
	public Pawn() {
		uid = uidCounter;
		uidCounter += 1;
	}
	
	public Pawn(final long uid) {
		this.uid = uid;
	}
	
	public void applySkeletonData(final SkeletonData data) {
		skeleton = new Skeleton(data);
		
		skeleton.updateWorldTransform();
		
		width = skeleton.getData().getWidth();
		height = skeleton.getData().getHeight();
		
		final float[] bounds = SkeletonBounds.calculateBounds(skeleton, tmpSkeletonBounds);
		
		xDiff = bounds[0];
		yDiff = bounds[1];
		width = bounds[2];
		height = bounds[3];
		
		extractMainAnimations(data);
	}

	private final void extractMainAnimations(final SkeletonData data) {
		for(final Animation animation : data.getAnimations()) {
			final String name = animation.getName();
			
			if(name.equalsIgnoreCase("Run3")) {
				runAnimation = animation;
			}
			else if(name.equalsIgnoreCase("Waiting")) {
				idleAnimation = animation;
			}
			else if(name.equalsIgnoreCase("Jump")) {
				jumpAnimation = animation;
			}
		}
	}
}
