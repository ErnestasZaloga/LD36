package com.company.minery.game.console;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.company.minery.game.Game;

public final class Console implements TextInput.Listener{
	
	private static final float OPACITY = 0.3f;
	private static Label log;
	
	public static final void log(final Object object) {
		if(log != null) {
			log.appendText(object.toString());
		}
	}
	
	private boolean active; /**/ public final boolean active() { return active; }
	
	protected final Game game;
	
	private final Stage stage;
	private final Group root;
	private InputProcessor inputProcessor;
	private Label timerLabel;
	
	private final Skin skin = new Skin(Gdx.files.internal("assets/console/uiskin.json"));
	
	private final TextInput textInput;
	
	private final CommandProcessor commandProcessor;
	
	public Console(final Game game) {
		this.game = game;
		
		stage = new Stage();
		root = new Group() {
			@Override
			public final void draw(final Batch batch, 
								   final float parentAlpha) {
				
				super.draw(batch, OPACITY);
			}
		};
		stage.addActor(root);
		
		timerLabel = new Label("", skin);
		timerLabel.setFontScale(1f);
		timerLabel.pack();
		
		textInput = new TextInput("", skin, this);
		
		root.addActor(textInput);
		
		log = new Label("", skin);
		log.setMaxLines(10);
		log.appendText("Welcome to the Developer Console!!!!!\n"
					  + "If you are not a developer GTFO!!! And have a nice day :)\n\n\n");
		
		log.setAlignment(Align.bottomLeft, Align.bottomLeft);
		
		root.addActor(log);
		
		commandProcessor = new CommandProcessor(this);
		
		timerLabel.setVisible(false);
		stage.addActor(timerLabel);
	}
	
	@Override
	public final void onLineEntered(final String text) {
		final String out = commandProcessor.process(text);
		
		log.appendText(out);
	}
	
	public final void setActive(final boolean active) {
		this.active = active;
		
		if(active) {
			inputProcessor = Gdx.input.getInputProcessor();
			
			Gdx.input.setInputProcessor(stage);
			stage.setKeyboardFocus(textInput);
		} 
		else {
			Gdx.input.setInputProcessor(inputProcessor);
		}
	}
	
	public final void updateAndRender(final float updateTime, 
									  final float renderTime) {
		
		if(timerLabel.isVisible()) {
			timerLabel.setText("Update: " + updateTime + " ms\nRender: " + renderTime + " ms");
			timerLabel.pack();
			timerLabel.setPosition(0f, root.getHeight() / 2f - timerLabel.getHeight() / 2f);
		}
		
		root.setVisible(active);
		
		stage.act();
		stage.draw();
	}
	
	public final void toggleTimer() {
		timerLabel.setVisible(!timerLabel.isVisible());
	}
	
	public final void setSize(final float width, 
							  final float height) {
		
		root.setSize(width, height);
		
		log.setSize(width, height - textInput.getHeight());
		log.setPosition(0, height - log.getHeight());
		
		textInput.setWidth(width);
	}
}
