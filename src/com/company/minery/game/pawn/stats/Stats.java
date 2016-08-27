package com.company.minery.game.pawn.stats;

public final class Stats {
	
	// *******************
	// DEPENDENT STATS
	// *******************
	private int level; /**/ public int level() { return level; }
	private int maxHealth; /**/ public int maxHealth() { return maxHealth; }
	private int maxEnergy; /**/ public int maxEnergy() { return maxEnergy; }
	
	// *******************
	// STAT STATES
	// *******************
	private int health; /**/ public int health() { return health; }
	private int energy; /**/ public int energy() { return energy; }
	
	// *******************
	// STATS
	// *******************
	private int exp; /**/ public int exp() { return exp; }
	private int vitality; /**/ public int vitality() { return vitality; }
	private int stamina; /**/ public int stamina() { return stamina; }
	private int statPoints; /**/ public int statPoints() { return statPoints; }
	private int totalStatPointsSpent; /**/ public int totalStatPointsSpent() { return totalStatPointsSpent; }
	
	public void setStatPoints(final int statPoints) {
		this.statPoints = statPoints;
	}
	
	public void spendPointOnVitality() {
		if(statPoints > 0) {
			statPoints -= 1;
			totalStatPointsSpent += 1;
			setVitality(vitality + 1);
		}
	}
	
	public void setVitality(final int vitality) {
		this.vitality = vitality;
		maxHealth = vitality * 10;
		
		if(health > maxHealth) {
			health = maxHealth;
		}
	}
	
	public void spendPointOnStamina() {
		if(statPoints > 0) {
			statPoints -= 1;
			totalStatPointsSpent += 1;
			setStamina(stamina + 1);
		}
	}
	
	public void setStamina(final int stamina) {
		this.stamina = stamina;
		maxEnergy = stamina * 10;
		
		if(energy > maxEnergy) {
			energy = maxEnergy;
		}
	}
	
	public void setExp(final int exp) {
		this.exp = exp;
		level = exp / 1000;
	}

	public void setHealth(final int health) {
		if(health < 0) {
			this.health = 0;
		}
		else if(health > maxHealth) {
			this.health = maxHealth;
		}
		else {
			this.health = health;
		}
	}

	public void setEnergy(final int energy) {
		if(energy < 0) {
			this.energy = 0;
		}
		else if(energy > maxEnergy) {
			this.energy = maxEnergy;
		}
		else {
			this.energy = energy;
		}
	}

}
