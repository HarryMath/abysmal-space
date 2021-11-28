package com.mikilangelo.abysmal.desktop;

import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.mikilangelo.abysmal.AbysmalSpace;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("abysmal space");
		config.setHdpiMode(HdpiMode.Logical);
		config.setWindowedMode(1200, 700);
		config.setResizable(true);
		glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
		Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
//		config.setFullscreenMode(displayMode);
		new Lwjgl3Application(new AbysmalSpace(false), config);

	}
}
