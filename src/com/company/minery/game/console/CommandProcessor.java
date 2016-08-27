package com.company.minery.game.console;


public final class CommandProcessor {
	private final Commands commands;
	
	private final StringBuilder sb = new StringBuilder();
	
	public CommandProcessor(final Console console) {
		commands = new Commands(console);
	}
	
	public final String process(final String input) {
		final String name = extractName(input);
		
		if(input.equals("")) {
			return " "; //Keep scrolling >:(
		} 
		else if(name.equalsIgnoreCase("help")) {
			return printCommands();
		}
		
		String commandOutput = "";
		
		boolean found = false;
		
		for(final Command c : commands) {
			if(c.name.equals(name)) {
				found = true;
				
				if(input.contains("-help")) {
					return c.helpMsg;
				}
				
				if(!name.equalsIgnoreCase(input)) {
					commandOutput = c.stringRunnable.run(c.params.setInput(input.substring(name.length())));
				}
				else {
					commandOutput = c.stringRunnable.run(c.params.setInput(input));
				} 
			}
		}
		
		return found ? (commandOutput + " ") : (name + " - command not found");
	}
	
	private final String extractName(final String string) {
		sb.setLength(0);
		
		boolean spaceInFront = true;
		
		for(int i = 0; i < string.length(); i++) {
			final char c = string.charAt(i);
			
			if(c == ' ' || c == '\r') {
				if(!spaceInFront) {
					return sb.toString();
				}
			} 
			else {
				spaceInFront = false;
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	private final String printCommands() {
		sb.setLength(0);
		
		for(final Command c : commands) {
			sb.append(c.name + " - " + c.helpMsg + '\n');
		}
		
		return sb.toString();
	}
}
