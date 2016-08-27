package com.company.minery.game.inventory;

import com.badlogic.gdx.utils.Array;

public class ItemDatabase {
	private final Array<Item> registeredItems; /**/ public final Array<Item> registeredItems() { return registeredItems; }
	private int id;
	
	public ItemDatabase() {
		registeredItems = new Array<Item>();
	}
	
	protected int registerItem(Item item) {
		registeredItems.add(item);
		item.setId(id);
		return id++;
	}
	
	public Item getItem(int id) {
		for(Item item : registeredItems) {
			if(item.id() == id) {
				return item.copy();
			}
		}
		throw new IllegalArgumentException("Item with id: " + id + " does not exist!");
	}
}
