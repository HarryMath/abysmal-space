package com.mikilangelo.abysmal.components;

import com.mikilangelo.abysmal.components.repositories.SoundsRepository;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public abstract class MusicPlayer {

  private static Music currentMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/default.mp3"));
  private static Music previousMusic;
  private static float currentVolume = 0;
  private static float previousVolume = -10;
  private static boolean isSwitching = false;

  public static void start(String path, final float volume) {
    start(SoundsRepository.getMusic(path), volume);
  }

  public static void start(Music music, final float volume) {
    if (previousVolume == -10) {
      currentMusic.setVolume(0);
    }
    previousMusic = currentMusic;
    currentMusic = music;
    previousVolume = (currentVolume + previousMusic.getVolume()) * 0.5f;
    currentVolume = volume;
    currentMusic.setLooping(true);
    currentMusic.setVolume(0);
    if (isSwitching) {
      return;
    }
    isSwitching = true;
  }

  public static void play() {
    if (currentMusic.getVolume() >= currentVolume) {
      return;
    }
    if (previousMusic.getVolume() > 0) {
      previousMusic.setVolume(Math.max(previousMusic.getVolume() * 0.98f - 0.00005f, 0));
    } if (previousMusic.getVolume() <= 0 && previousMusic.isPlaying()) {
      previousMusic.pause();
      previousMusic.setPosition(0);
    }
    if (currentMusic.isPlaying() || previousMusic.getVolume() <= previousVolume * 0.35f) {
      currentMusic.setVolume( Math.max((currentMusic.getVolume() * 160 + currentVolume) / 161f, 0) );
      if (!currentMusic.isPlaying()) {
        currentMusic.play();
      }
    }
    if (currentMusic.getVolume() >= currentVolume * 0.999f - 0.0001f) {
      currentMusic.setVolume(currentVolume);
    }
  }
}
