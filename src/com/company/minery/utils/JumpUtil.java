package com.company.minery.utils;

public final class JumpUtil {
	
	// the object’s position, x; 
	// the object’s initial velocity, v0; 
	// the object’s final velocity, v; 
	// the object’s acceleration, a; 
	// and the elapsed time, t
	
	public static float jumpHeight(final float v0, 
								   final float a) {
		
		return x(0, 0, v0, t(0, v0, a));
	}
	
	public static float jumpHeightInTime(final float v0, 
										 final float a, 
										 final float t) {
		
		return x(0, v(v0, a, t), v0, t);
	}
	
	public static float jumpDuration(final float v0, 
									 final float a) {
		
		return t(-v0, v0, a);
	}
	
	public static float fallDuration(final float v0, 
									 final float x, 
									 final float x0, 
									 final float a) {
		
		final float v = v(v0, x, x0, a);
		final float t = t(v, v0, a);
		
		return t;
	}
	
	public static float fallDurationWithSpeedLimit(final float v0, 
												   final float vMax, 
												   final float x, 
												   final float x0, 
												   final float a) {
		
		final float t1 = t(vMax, v0, a);
		final float x1 = x(x0, vMax, v0, t1);

		if(x1 > x) {
			final float t =  t(v0, x, x0, a);
			return t;
		}

		final float dst = Math.abs(x - x1);
		final float t2 = dst / vMax;

		return t1 + t2;
	}
	
	public static float speedAtTime(final float v0, 
									final float vMax, 
									final float t, 
									final float a) {
		
		final float v = Math.abs(v(v0, a, t));
		return Math.min(v, vMax);
	}
	
	private static float x(final float x0,
						   final float v, 
						   final float v0, 
						   final float t) {
		
		return x0 + (v + v0) * t / 2f;
	}
	
	private static float v(final float v0, 
						   final float a, 
						   final float t) {
		
		return v0 + a * t;
	}
	
	private static float v(final float v0, 
						   final float x, 
						   final float x0, 
						   final float a) {
		
		return (float) Math.sqrt(v0 * v0 + 2 * a * (x - x0));
	}
	
	private static float t(final float v, 
						   final float v0, 
						   final float a) {
		
		return Math.abs((v - v0) / a);
	}
	
	private static float t(final float v0,
	         			   final float x,
	         			   final float x0,
	         			   final float a) {

		final float d = v0 * v0 - 2 * a * (x - x0);

		if(d == 0) {
			return -v0 / a;
		} 
		else if(d > 0) {
			final float res1 = (float) ((-v0 + Math.sqrt(d)) / a);
			final float res2 = (float) ((-v0 - Math.sqrt(d)) / a);

			return Math.max(res1, res2);
		}

		return Float.NaN;
	}
	
}
