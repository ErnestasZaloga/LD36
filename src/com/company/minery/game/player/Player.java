package com.company.minery.game.player;

import com.company.minery.game.GameAssets;
import com.company.minery.game.GameAssets.TextureRegionExt;

public final class Player extends PhysicalObject implements InputTranslator.PlayerInputListener {
	
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
	
	public final boolean local;
	public boolean flip;
	
	public boolean requestsAttack;
	public float attackX;
	public float attackY;
	
	public boolean hasWeapon = true;
	
	public TextureRegionExt region;
	
	//Main animations
	//private Animation idleAnimation; /**/ public final Animation idleAnimation() { return idleAnimation; }
	//private Animation jumpAnimation; /**/ public final Animation jumpAnimation() { return jumpAnimation; }
	//private Animation runAnimation; /**/ public final Animation runAnimation() { return runAnimation; }
	
	public Player(final boolean local) {
		this.local = local;
	}
	
	public Player(final boolean local, 
				  final long uid) {
		
		super(uid);
		this.local = local;
	}
	
	@Override
	public void onLeftPressed() {
		movementDirection = MovementDirection.Left;
	}

	@Override
	public void onRightPressed() {
		movementDirection = MovementDirection.Right;
	}

	@Override
	public void onJumpPressed() {
		requestsJump = true;
	}
	
	@Override
	public void onIdle() {
		movementDirection = MovementDirection.Idle;
	}
	
	@Override
	public void onAttackPressed(final float dirX, final float dirY) {
		System.out.println("REQUESTS ATTACK");
		
		requestsAttack = true;
		attackX = dirX;
		attackY = dirY;
	}
	
	public void flip(final boolean flip) {
		if(this.flip != flip) {
			this.flip = flip;
			this.region.flip(true, false);
		}
	}
	
	@Override
	public void applyAppearance(final GameAssets assets) {
		region = assets.characterBody;
		width = region.getWidth();
		height = region.getHeight();
	}

}
