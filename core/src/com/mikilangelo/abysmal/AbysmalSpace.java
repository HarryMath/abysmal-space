package com.mikilangelo.abysmal;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikilangelo.abysmal.components.MusicPlayer;
import com.mikilangelo.abysmal.components.ShipDefinitions;
import com.mikilangelo.abysmal.components.repositories.SoundsRepository;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.models.game.extended.Asteroid;
import com.mikilangelo.abysmal.ui.screens.GameScreen;
import com.mikilangelo.abysmal.ui.screens.LoadingScreen;

public class AbysmalSpace extends Game {
	public OrthographicCamera cameraInterface;
	public SpriteBatch batchInterface;
	public SpriteBatch backgroundBatch;
	public SpriteBatch objectsBatch;
	public BitmapFont customFont;
	public BitmapFont simpleFont;
	public final boolean isSensor;

	public AbysmalSpace(boolean isSensor) {
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
		simpleFont = new BitmapFont();
		if (!isSensor) {
			Gdx.graphics.setVSync(true);
		}
		simpleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		this.setScreen(new LoadingScreen(this));
	}

	@Override
	public void render() {
		ScreenUtils.clear(0, 0, 0, 1);
		MusicPlayer.play();
		super.render();
	}

	@Override
	public void dispose() {
		super.dispose();
		objectsBatch.dispose();
		batchInterface.dispose();
		backgroundBatch.dispose();
		customFont.dispose();
		simpleFont.dispose();
		ShipDefinitions.disposeAll();
		TexturesRepository.disposeAll();
		SoundsRepository.disposeAll();
		Asteroid.dispose();
		try {
			GameScreen.enemiesProcessor.dispose();
		} catch (Exception ignore) {
			System.exit(0);
		}
	}
}
