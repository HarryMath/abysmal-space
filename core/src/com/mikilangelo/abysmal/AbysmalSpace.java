package com.mikilangelo.abysmal;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikilangelo.abysmal.screens.game.controllers.GameController;
import com.mikilangelo.abysmal.shared.MusicPlayer;
import com.mikilangelo.abysmal.shared.ShipDefinitions;
import com.mikilangelo.abysmal.shared.repositories.SoundsRepository;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.screens.game.actors.fixtures.Asteroid;
import com.mikilangelo.abysmal.screens.loading.LoadingScreen;

public class AbysmalSpace extends Game {
	public OrthographicCamera cameraInterface;
	public SpriteBatch batchInterface;
	public SpriteBatch backgroundBatch;
	public SpriteBatch objectsBatch;
	public BitmapFont customFont;
	public BitmapFont digits;
	public BitmapFont simpleFont;
	public final boolean isSensor;
	public final GameController controller;

	public AbysmalSpace(GameController controller, boolean isSensor) {
		this.controller = controller;
		this.isSensor = isSensor;
	}

	@Override
	public void create() {
		objectsBatch = new SpriteBatch();
		backgroundBatch = new SpriteBatch();
		batchInterface = new SpriteBatch();
		cameraInterface = new OrthographicCamera();
		cameraInterface.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batchInterface.setProjectionMatrix(cameraInterface.combined);
		customFont = new BitmapFont(Gdx.files.internal("fonts/Pixellari.fnt"));
		digits = new BitmapFont(Gdx.files.internal("fonts/digits.fnt"));
		simpleFont = new BitmapFont();
		if (!isSensor) {
			Gdx.graphics.setVSync(true);

		}
		// Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
		simpleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		this.setScreen(new LoadingScreen(this));
	}

	@Override
	public void render() {
		ScreenUtils.clear(0, 0, 0, 1);
		super.render();
	}

	@Override
	public void dispose() {
		super.dispose();
		this.screen.dispose();
		objectsBatch.dispose();
		batchInterface.dispose();
		backgroundBatch.dispose();
		customFont.dispose();
		simpleFont.dispose();
		MusicPlayer.dispose();
		ShipDefinitions.disposeAll();
		TexturesRepository.disposeAll();
		SoundsRepository.disposeAll();
		Asteroid.dispose();
		Gdx.app.exit();
		System.exit(0);
	}
}
