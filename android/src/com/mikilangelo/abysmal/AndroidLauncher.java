package com.mikilangelo.abysmal;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mikilangelo.abysmal.AbysmalSpace;
import com.mikilangelo.abysmal.controller.SensorController;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new AbysmalSpace(new SensorController(), true), config);
	}
}
