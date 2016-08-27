package com.company.minery.game.inventory;

import com.badlogic.gdx.utils.Array;

public class Inventory {
	
	private int copper; /**/ public final int copper() { return copper; }
	
	private Item miningItem; /**/ public final Item miningItem() { return miningItem; }
	private Item mainHandWeapon; /**/ public final Item mainHandWeapon() { return mainHandWeapon; }
	private final Array<Item> baggedItems = new Array<Item>(); /**/ public final Array<Item> baggedItems() { return baggedItems; }
	
	public Inventory() {
		
	}
	
}
