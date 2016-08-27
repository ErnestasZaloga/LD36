package com.company.minery.game.console;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;

public class TextInput extends TextField{
	
	protected interface Listener {
		public void onLineEntered(final String string);
	}
	
	private static final int HISTORY_SIZE = 50;
	
	private final Array<String> lines = new Array<String>() {
		@Override
		public void insert(int index, String value) {
			super.insert(index, value);
			while(size > HISTORY_SIZE) {
				lines.pop();
			}
		}
	};
	
	private int lineIndex = 0;

	public TextInput(final String text,
					 final Skin skin,
					 final Listener listener) {
		
		super(text, skin);
		
		setBlinkTime(0.5f);
		
		setTextFieldFilter(new TextFieldFilter() {
			
			@Override
			public final boolean acceptChar(final TextField textField, 
											final char c) {
				
				return c != '`';
			}
		});
		
		setTextFieldListener(new TextFieldListener() {
			
			@Override
			public final void keyTyped(final TextField textField, 
									   final char c) {
				
				//if Enter
				if(c == '\r') {
					final String text = textField.getText();
					
					if(!text.equals("")) {
						int tmp = lines.indexOf(text, false);
						
						if(tmp != -1) {
							lines.insert(0, lines.removeIndex(tmp));
						} 
						else {
							lines.insert(0, text);
						}
					}
					
					listener.onLineEntered(text);
					textField.setText("");
					
					lineIndex = 0;
				}
			}
		});
		
		addListener(new InputListener() {
			private boolean downPressed;
			private boolean upPressed;
			
			@Override
			public boolean keyDown(InputEvent event, int keyCode) {
				if(lines.size == 0 || (keyCode != Keys.UP && keyCode != Keys.DOWN)) {
					downPressed = false;
					upPressed = false;
					return false;
				}
				
				if(keyCode == Keys.UP) {
					if(downPressed) {
						downPressed = false;
						lineIndex += 2;
					}
					
					lineIndex = MathUtils.clamp(lineIndex, 0, lines.size - 1);
					
					setText(lines.get(lineIndex++));
					
					upPressed = true;
				}
				else if(keyCode == Keys.DOWN) {
					if(upPressed) {
						upPressed = false;
						lineIndex -= 2;
					}
					
					lineIndex = MathUtils.clamp(lineIndex, 0, lines.size - 1);
					
					setText(lines.get(lineIndex--));
					
					downPressed = true;
				}
				
				setCursorPosition(getText().length());
				
				return false;
			}
		});
	}

}
