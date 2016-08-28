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
	
	public Node body;
	public Node head;
	public Node leftHand;
	public Node rightHand;
	public Node leftFoot;
	public Node rightFoot;
	
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
			this.body.texture.flip(true, false);
			this.head.texture.flip(true, false);
//			this.leftHand.texture.flip(true, false);
			this.rightHand.texture.flip(true, false);
			this.leftFoot.texture.flip(true, false);
//			this.rightFoot.texture.flip(true, false);
			
			float center = this.body.texture.getWidth()/2;
//			this.leftFoot.offsetX = ;
		}
	}
	
	@Override
	public void applyAppearance(final GameAssets assets) {
		body = new Node(0, assets.characterFoot.getHeight()*2, assets.characterBody.getWidth(), assets.characterBody.getHeight(), 0, assets.characterBody);
		head = new Node((body.width-assets.characterHead.getWidth())/2-(assets.characterHead.getWidth()*0.005f), body.height-assets.characterHead.getHeight()*0.1f+assets.characterFoot.getHeight()*2, assets.characterHead.getWidth(), assets.characterHead.getHeight(), 0, assets.characterHead);
		leftHand = new Node(body.width, body.height*0.5f + assets.characterFoot.getHeight()*2, assets.characterFist.getWidth(), assets.characterFist.getHeight(), 0, assets.characterFist);
		rightHand = new Node(-1*assets.characterFist.getWidth(), body.height*0.5f + assets.characterFoot.getHeight()*2, assets.characterFist.getWidth(), assets.characterFist.getHeight(), 0, assets.characterFist);
		leftFoot = new Node(assets.characterFoot.getWidth()*1.3f, 0, assets.characterFoot.getWidth(), assets.characterFoot.getHeight(), 0, assets.characterFoot);
		rightFoot = new Node(assets.characterFoot.getWidth()*0.1f, 0, assets.characterFoot.getWidth(), assets.characterFoot.getHeight(), 0, assets.characterFoot);
		width = body.texture.getWidth();
		height = body.texture.getHeight();
	}

}
