package com.company.minery.game.inventory;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class Item implements Poolable{
	
	public static final class Builder {
		private final Pool<Item> itemPool = new Pool<Item>() {
			@Override
			protected final Item newObject() {
				return new Item(itemPool);
			}
		};
		
		private Slot slot;
		private int damage;
		private int armor;
		
		public final Builder setSlot(final Slot slot) {
			this.slot = slot;
			return this;
		}

		public final Builder setDamage(final int damage) {
			this.damage = damage;
			return this;
		}

		public final Builder setArmor(final int armor) {
			this.armor = armor;
			return this;
		}
		
		public final Item build() {
			Item item = itemPool.obtain();
			
			item.setAll(slot, damage, armor);
			
			slot = null;
			damage = -1;
			armor = -1;
			
			return item;
		}
	}
	
	private final Pool<Item> itemPool;
	private int id; /**/ public final int id() { return id; }
	
	private Slot slot; /**/ public final Slot slot() { return slot; }
	
	//*******************************
	//Equipable properties
	//*******************************
	
	//Weapon
	private int damage; /**/ public final int damage() { return damage; }
	
	//Mining tool
	
	//Armor
	private int armor; /**/ public final int armor() { return armor; }
	
	private Item(final Pool<Item> itemPool) {
		this.itemPool = itemPool;
	}
	
	protected void setId(final int id) {
		this.id = id;
	}
	
	private void setAll(final Slot slot,
					    final int damage,
						final int armor) {
		
		this.slot = slot;
		this.damage = damage;
		this.armor = armor;
	}
	
	public void setSlot(final Slot slot) {
		this.slot = slot;
	}

	public void setDamage(final int damage) {
		this.damage = damage;
	}
	
	public void setArmor(final int armor) {
		this.armor = armor;
	}
	
	public Item copy() {
		Item copy = itemPool.obtain();
		copy.setAll(slot, damage, armor);
		return copy;
	}
	
	public void destroy() {
		itemPool.free(this);
	}

	@Override
	public void reset() {
		slot = null;
	}
}
