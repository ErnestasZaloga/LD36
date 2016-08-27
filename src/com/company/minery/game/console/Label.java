package com.company.minery.game.console;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Label extends com.badlogic.gdx.scenes.scene2d.ui.Label{
	
	private int maxLines;
	private int currLines;

	public Label(final CharSequence text, final Skin skin) {
		super(text, skin);
		maxLines = 10;
	}
	
	public final void appendText(final String line) {
		final int nl = findNewLineCount(line);
		currLines += nl + 1;
		
		clearLines();
		
		setText(getText() + "\n" + line);
		pack();
		
		currLines = findNewLineCount(getText().toString());
	}
	
	public final void setMaxLines(final int maxLines) {
		this.maxLines = maxLines - 1;
		
		clearLines();
	}
	
	private final void clearLines() {
		if(currLines <= maxLines) {
			return;
		}
		String text = getText().toString();
		
		final int n = currLines - maxLines;
		currLines -= n;
		
		for(int i = 0; i < n; i++) {
			text = clearFirstLine(text);
		}
		
		setText(text);
		pack();
	}
	
	private final String clearFirstLine(final String s) {
		int substr = 0;
		
		for(int i = 0, n = s.length(); i < n; i++) {
			if(s.charAt(i) == '\n') {
				substr++;
				return s.substring(substr);
			}
			substr++;
		}
		return s.substring(substr);
	}
	
	private final int findNewLineCount(final String s) {
		int count = 0;
		
		for(int i = 0, n = s.length(); i < n; i++) {
			if(s.charAt(i) == '\n') {
				count++;
			}
		}
		
		return count;
	}
}
