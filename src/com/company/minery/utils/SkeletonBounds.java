package com.company.minery.utils;

import com.badlogic.gdx.utils.Array;
import com.company.minery.utils.spine.Skeleton;
import com.company.minery.utils.spine.Slot;
import com.company.minery.utils.spine.attachments.Attachment;
import com.company.minery.utils.spine.attachments.MeshAttachment;
import com.company.minery.utils.spine.attachments.RegionAttachment;
import com.company.minery.utils.spine.attachments.SkinnedMeshAttachment;

public class SkeletonBounds {

	/**Calculates skeleton bottom left coordinates and size.
	 * @return float[x, y, width, height].*/
	public static final float[] calculateBounds(final Skeleton skeleton,
												final float[] out) {
		
		final Array<Slot> slots = skeleton.getSlots();
		
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxRight = Float.MIN_VALUE;
		float maxTop = Float.MIN_VALUE;
		
		for(final Slot slot : slots) {
			final Attachment att = slot.getAttachment();
			
			float[] coords;
			
			if(att instanceof RegionAttachment) {
				final RegionAttachment attachment = ((RegionAttachment) att);
				attachment.updateWorldVertices(slot, false);
				coords = attachment.getWorldVertices();
			} 
			else if(att instanceof MeshAttachment) {
				final MeshAttachment attachment = ((MeshAttachment) att);
				attachment.updateWorldVertices(slot, false);
				coords = attachment.getWorldVertices();
			}
			else if(att instanceof SkinnedMeshAttachment) {
				final SkinnedMeshAttachment attachment = ((SkinnedMeshAttachment) att);
				attachment.updateWorldVertices(slot, false);
				coords = attachment.getWorldVertices();
			}
			else {
				continue;
			}
			
			for(int i = 0, n = coords.length; i < n; i += 5) {
				final float x = coords[i];
				final float y = coords[i + 1];
				
				minX = Math.min(minX, x);
				minY = Math.min(minY, y);
				maxRight = Math.max(maxRight, x);
				maxTop = Math.max(maxTop, y);
			}
		}
		
		out[0] = -minX;
		out[1] = -minY;
		out[2] = maxRight - minX;
		out[3] = maxTop - minY; 
		
		return out;
	}
}

