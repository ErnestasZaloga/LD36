package com.company.minery.game.console;

import java.net.InetAddress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.company.minery.game.console.Command.Parameters;
import com.company.minery.game.console.Command.StringRunnable;

public final class Commands extends Array<Command>{
	
	public Commands(final Console console) {
		final Command.Builder builder = new Command.Builder();
		
		//Add commands here
		add(builder
				.name("timer")
				.helpMsg("enable/disable timer.")
				.stringRunnable(new StringRunnable() {
					@Override
					public final String run(final Parameters params) {
						console.toggleTimer();
						return "Timer toggled";
					}
				})
			.build());
		add(builder
				.name("fullscreen")
				.helpMsg("toggles fullscreen on or off, no arguments.")
				.stringRunnable(new StringRunnable() {
					private boolean fullscreen = true;
					
					@Override
					public String run(Parameters params) {
						fullscreen = !fullscreen;
						if(fullscreen) {
							Gdx.graphics.setDisplayMode(800, 600, false);
						} else {
							Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode());
						}
						
						return "";
					}
				})
			.build());
		add(builder
				.name("ip")
				.helpMsg("prints out inner ip address")
				.stringRunnable(new StringRunnable() {
					@Override
					public String run(final Parameters params) {
						try {
							final InetAddress address = InetAddress.getLocalHost();
							return address.getHostAddress();
						}
						catch(final Exception ex) {
							return "failed to fetch ip address";
						}
					}
				})
			.build());
		add(builder
				.name("disconnect")
				.helpMsg("disconnects you from the server if you are connected")
				.stringRunnable(new StringRunnable() {
					@Override
					public String run(final Parameters params) {
						console.game.switchToLocal();
						return "switch attempted, view the java console for output";
					}
				})
			.build());
		add(builder
				.name("connect")
				.helpMsg("connects you to the specified ip. Syntax: connect 192.0.0.1")
				.stringRunnable(new StringRunnable() {
					@Override
					public String run(final Parameters params) {
						console.game.switchToRemote(params.nextString());
						return "connection attempted, view the java console for output";
					}
				})
			.build());
		add(builder
				.name("cp")
				.helpMsg("connects you to 192.168.1.214")
				.stringRunnable(new StringRunnable() {
					@Override
					public String run(final Parameters params) {
						console.game.switchToRemote("192.168.1.214");
						console.setActive(false);
						return "connection attempted, view the java console for output";
					}
				})
			.build());
		
		/*add(builder
				.name("test")
				.helpMsg("string test")
				.stringRunnable(new StringRunnable() {
					@Override
					public String run(Parameters params) {
						return params.nextString() + " " + params.nextFloat();
					}
				})
			.build());*/
	}
}
