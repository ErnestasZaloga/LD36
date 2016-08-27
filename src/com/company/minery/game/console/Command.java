package com.company.minery.game.console;

public final class Command {
	
	public static final class Builder {
		private String name;
		private String helpMsg;
		private StringRunnable sr;
		
		public final Builder name(final String name) {
			this.name = name;
			return this;
		}
		
		public final Builder helpMsg(final String msg) {
			this.helpMsg = msg;
			return this;
		}
		
		public final Builder stringRunnable(final StringRunnable sr) {
			this.sr = sr;
			return this;
		}
		
		public final Command build() {
			if(name == null || name.equals("")) {
				throw new IllegalArgumentException("Command name not set!");
			} else if (sr == null) {
				throw new IllegalArgumentException("Callback not set");
			}
			
			final Command c = new Command(name, helpMsg, sr);
			
			name = null;
			helpMsg = "";
			sr = null;
			
			return c;
		}
	}
	
	public interface StringRunnable {
		public String run(final Parameters params);
	}
	
	public final class Parameters {
		private String input;
		private final StringBuilder sb = new StringBuilder();

		public final Parameters setInput(final String input) {
			this.input = input;
			return this;
		}
		
		public final float nextFloat() {
			float f = 0;
			
			try {
				f =  Float.parseFloat(nextString());
			} 
			catch(final Exception ex) {
				return Float.NaN;
			}
			
			return f;
		}
		
		public final String nextString() {
			final String input = this.input;
			sb.setLength(0);
			
			boolean spaceInFront = true;
			int spaceInFrontCount = 0;
			boolean kabutString = false;
			
			for(int i = 0, n = input.length(); i < n; i++) {
				final char c = input.charAt(i);
				if(c == ' ') {
					
					if(!spaceInFront && !kabutString) {
						this.input = input.substring(sb.length() + spaceInFrontCount);
						return sb.toString();
					}
					
					if(kabutString) {
						sb.append(c);
					}
					
					if(spaceInFront) {
						spaceInFrontCount++;
					}
					
					continue;
				}
				else if(c == '"') {
					if(kabutString) {
						this.input = input.substring(sb.length() + 1 + spaceInFrontCount);
						return sb.toString();
					}
					kabutString = true;
					spaceInFront = false;
				}
				else {
					spaceInFront = false;
					sb.append(c);
				}
			}
			
			this.input = input.substring(sb.length() + spaceInFrontCount);
			return sb.toString();
		}
	}
	
	public final String name;
	public final String helpMsg;
	public final StringRunnable stringRunnable;
	public final Parameters params = new Parameters();
	
	private Command(final String name,
					final String helpMsg,
					final StringRunnable sr) {
		
		this.name = name;
		this.helpMsg = helpMsg;
		this.stringRunnable = sr;
	}
}
