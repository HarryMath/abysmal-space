package com.mikilangelo.abysmal.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.mikilangelo.abysmal.AbysmalSpace;
import com.mikilangelo.abysmal.desktop.controller.DesktopController;

import java.lang.reflect.Field;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("abysmal space");
		config.setHdpiMode(HdpiMode.Logical);
		config.setWindowedMode(1200, 700);
		config.setResizable(true);
		Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
		// config.setFullscreenMode(displayMode);
		config.setDecorated(true);
		new Thread(() -> {
			try {
				Thread.sleep(1000);
			} catch (Exception ignore) {}
			try {
				Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
				Field windowField = Lwjgl3Application.class.getDeclaredField("currentWindow");
				windowField.setAccessible(true);
				Lwjgl3Window window = (Lwjgl3Window) windowField.get(app);
				window.maximizeWindow();
				// app.getGraphics().setUndecorated(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
		new Lwjgl3Application(new AbysmalSpace(new DesktopController(),false), config);

	}
}
