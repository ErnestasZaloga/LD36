package com.company.minery.game.player;

import com.company.minery.Constants;
import com.company.minery.game.GameAssets;

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
	
	public static enum Pose {
		
		Idle(true),
		Run(true),
		Jump(false);
		
		public final boolean animated;
		
		private Pose(final boolean animated) {
			this.animated = animated;
		}
		
	}
	
	public final boolean local;
	public boolean flip;
	
	public boolean requestsAttack;
	public float attackX;
	public float attackY;
	
	public boolean hasWeapon = true;
	
	public final Node body = new Node();
	public final Node head = new Node();
	public final Node leftHand = new Node();
	public final Node rightHand = new Node();
	public final Node leftFoot = new Node();
	public final Node rightFoot = new Node();
	
	public float animationTimer;
	private Pose lastPose = Pose.Idle;
	
	public int lives = Constants.LIVES;
	public long ownSpearUid;
	public boolean ignoreOwnSpear;
	
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
	
	public void onWeaponTaken() {
		this.hasWeapon = true;
		this.initialPose();
	}
	
	public void onWeaponLost() {
		this.hasWeapon = false;
		this.initialPose();
	}
	
	public void flip(final boolean flip) {
		if(this.flip != flip) {
			this.flip = flip;
			flipNodes();
		}
	}
	
	private void flipNodes() {
		final float center = this.width / 2;
		
		head.flip(center);
		body.flip(center);
		leftHand.flip(center);
		rightHand.flip(center);
		leftFoot.flip(center);
		rightFoot.flip(center);
	}
	
	@Override
	public void applyAppearance(final GameAssets assets) {
		leftFoot.texture = assets.characterFoot;
		rightFoot.texture = assets.characterFoot;
		body.texture = assets.characterBody;
		head.texture = assets.characterHead;
		leftHand.texture = assets.characterFist;
		rightHand.texture =  assets.characterFist;
		
		leftFoot.originX = leftFoot.texture.getWidth() / 2f;
		leftFoot.originY = leftFoot.texture.getHeight() / 2f;
		
		rightFoot.originX = rightFoot.texture.getWidth() / 2f;
		rightFoot.originY = rightFoot.texture.getHeight() / 2f;
		
		body.originX = body.texture.getWidth() / 2f;
		body.originY = body.texture.getHeight() / 2f;
		
		head.originX = head.texture.getWidth() / 2f;
		head.originY = head.texture.getHeight() / 2f;
		
		rightHand.originX = rightHand.texture.getWidth() / 2f;
		rightHand.originY = rightHand.texture.getHeight() / 2f;
		
		leftHand.originX = leftHand.texture.getWidth() / 2f;
		leftHand.originY = leftHand.texture.getHeight() / 2f;
		
		initialPose();
		
		width = body.texture.getWidth();
		height = head.offsetY + head.texture.getHeight();
	}
	
	public void initialPose() {
		leftFoot.offsetX = leftFoot.texture.getWidth() * 1.3f;
		leftFoot.offsetY = 0;
		leftFoot.rotation = 0;
		
		rightFoot.offsetX = rightFoot.texture.getWidth() * 0.1f;
		rightFoot.offsetY = 0;
		rightFoot.rotation = 0;
		
		body.offsetX = 0;
		body.offsetY = leftFoot.texture.getHeight() * 1.3f;
		body.rotation = 0;
		
		head.offsetX = (body.texture.getWidth() - head.texture.getWidth()) / 2 - (head.texture.getWidth() * 0.01f);
		head.offsetY = body.texture.getHeight() - head.texture.getHeight() * 0.37f + leftFoot.texture.getHeight() * 2;
		head.rotation = 0;
		
		leftHand.offsetX = body.texture.getWidth();
		leftHand.offsetY = body.texture.getHeight() * 0.27f + leftFoot.texture.getHeight() * 2;
		leftHand.rotation = 0;
		
		rightHand.offsetX = -1 * rightHand.texture.getWidth();
		rightHand.offsetY = body.texture.getHeight() * 0.27f + leftFoot.texture.getHeight() * 2;
		rightHand.rotation = 0;
		
		if(this.hasWeapon) {
			rightHand.offsetY += body.texture.getHeight() - rightHand.originY;
			rightHand.rotation = 10f;
		}
		
		if(flip) {
			flipNodes();
		}
	}
	
	public void jumpPose() {
		initialPose();
		
		final float perc = body.texture.getHeight() * 0.05f;
		final float armSwingAmount = perc * 3f;
		
		final float hmod = flip ? -1 : 1;
		leftHand.offsetX += hmod * armSwingAmount;
		rightHand.offsetX -= hmod * armSwingAmount;
		final float footTopAmount = perc * 1f;
		rightFoot.offsetY -= footTopAmount;
	}
	
	public void stepAnimation(final float delta) {
		animationTimer += delta;
		
		final Pose currentPose = findPose();
		
		if(currentPose != this.lastPose) {
			if(currentPose == Pose.Jump) {
				jumpPose();
			}
			else {
				initialPose();
			}
			
			animationTimer = 0f;
		}
		
		this.lastPose = currentPose;
		
		if(currentPose.animated) {
			if(currentPose == Pose.Idle) {
				final boolean downPhase = animationTimer % Constants.IDLE_ANIMATION_DURATION >= Constants.IDLE_ANIMATION_DURATION / 2;
				
				initialPose();
				
				if(downPhase) {
					final float amount = body.texture.getHeight() * 0.04f;
					body.offsetY -= amount;
					head.offsetY -= amount * 0.6f;
					
					rightHand.offsetY -= amount * 0.6f;
					leftHand.offsetY -= amount * 0.6f;
				}
			}
			else if(currentPose == Pose.Run) {
				final float half = Constants.RUN_ANIMATION_DURATION / 2;
				final float bounded = animationTimer % Constants.RUN_ANIMATION_DURATION;
				final boolean backPhase = bounded >= half;
				initialPose();
				
				final float perc = body.texture.getHeight() * 0.05f;
				final float footTopAmount = perc * 1f;
				final float armSwingAmount = perc * 3f;
				final float footSwingAmount = perc * 5f;
				final float bodyAmount = body.texture.getHeight() * 0.04f;
				
				final float hmod = flip ? -1 : 1;
				
				if(backPhase) {
					// Right foot is going down, left is going up
					leftFoot.offsetY += footTopAmount;
					rightFoot.offsetX -= hmod * footSwingAmount;
					
					body.offsetY -= bodyAmount;
					head.offsetY -= bodyAmount * 0.6f;
					
					rightHand.offsetY -= bodyAmount * 0.6f;
					leftHand.offsetY -= bodyAmount * 0.6f;
				}
				else {
					rightFoot.offsetY += footTopAmount;
					leftFoot.offsetX -= hmod * footSwingAmount;
				}
			}
			else if(currentPose == Pose.Jump) {
				jumpPose();
			}
		}
	}
	
	private Pose findPose() {
		if(this.isInAir || this.isJumping) {
			return Pose.Jump;
		}
		else {
			return this.movementDirection == MovementDirection.Idle ? Pose.Idle : Pose.Run;
		}
	}
	
}
