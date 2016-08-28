package com.company.minery.game.player;

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

}
