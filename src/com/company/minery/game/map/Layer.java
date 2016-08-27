package com.company.minery.game.map;


public final class Layer {

	public final Tiles tiles;
	private StaticDecoration[] decorations; /**/ public final StaticDecoration[] decorations() { return decorations; }
	
	public Layer(final Tiles tiles, 
				 final StaticDecoration[] decorations) {
		
		this.tiles = tiles;
		this.decorations = decorations;
	}
	
	protected void setDecorations() {
		this.decorations = decorations;
	}
	
	public void setScale(final float rescale) {
		// Rescale decorations
		final StaticDecoration[] decorations = this.decorations;
		
		if(decorations != null) {
			final int n = decorations.length;
			
			for(int i = 0; i < n; i += 1) {
				//decorations[i].setScale(rescale);
			}
		}
	}
}
