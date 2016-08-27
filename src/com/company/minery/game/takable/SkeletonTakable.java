package com.company.minery.game.takable;

import com.company.minery.utils.SkeletonBounds;
import com.company.minery.utils.spine.Skeleton;

public class SkeletonTakable extends Takable {
	
	private static final float[] tmpSkeletonBounds = new float[4];
	private Skeleton skeleton; /**/ public final Skeleton skeleton() { return skeleton; }

	private float xDiff; /**/ public final float xDiff() { return xDiff; }
	private float yDiff; /**/ public final float yDiff() { return yDiff; }

	public void setTakableSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
		
		skeleton.updateWorldTransform();
		
		final float[] bounds = SkeletonBounds.calculateBounds(skeleton, tmpSkeletonBounds);
		
		xDiff = bounds[0];
		yDiff = bounds[1];
		setWidth(bounds[2]);
		setHeight(bounds[3]);
	}
	
	@Override
	public void reset() {
		super.reset();
		
		skeleton = null;
		xDiff = 0;
		yDiff = 0;
	}
}
