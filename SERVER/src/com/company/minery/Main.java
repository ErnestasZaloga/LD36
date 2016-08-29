package com.company.minery;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		HeadlessApplicationConfiguration cfg = new HeadlessApplicationConfiguration();
		new HeadlessApplication(new App(), cfg);
	}
}
