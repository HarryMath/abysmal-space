package com.mikilangelo.abysmal.shared;

import com.mikilangelo.abysmal.shared.repositories.SoundsRepository;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.mikilangelo.abysmal.shared.tools.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MusicPlayer {

  private static Music currentMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/default.mp3"));
  private static Music previousMusic = null;
  private static float currentVolume = 0;
  private static float previousVolume = -10;
  private static boolean isSwitching = false;
  private static final VolumeThread volumeThread = new VolumeThread();
  static {
    volumeThread.start();
  }


  public static void start(String path, final float volume) {
    start(SoundsRepository.getMusic(path), volume);
  }

  public static void start(Music music, final float volume) {
    if (previousVolume == -10) {
      currentMusic.setVolume(0);
    }
    if (previousMusic != null && previousMusic.isPlaying()) {
      previousMusic.stop();
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

  public static void stopMusicFade() {
    currentMusic.setVolume(currentVolume);
    if (previousMusic != null) {
      previousMusic.stop();
    }
  }

  public static void dispose() {
    volumeThread.interrupt();
    currentVolume = -1;
    currentMusic.stop();
    previousMusic.stop();
    currentMusic.dispose();
    previousMusic.dispose();
  }

  private static class VolumeThread extends Thread {

    private final AtomicBoolean playerActive = new AtomicBoolean(true);

    @Override
    public void run() {
      while (playerActive.get()) {
        try {
          updateMusic();
          sleep(17);
        } catch (InterruptedException ignore) {}
      }
      Logger.log(this, "run", "ended");
    }

    private void updateMusic() {
      if (currentVolume < 0 || currentMusic.getVolume() >= currentVolume) {
        return;
      }
      if (previousMusic.getVolume() > 0) {
        previousMusic.setVolume(Math.max(previousMusic.getVolume() * 0.98f - 0.00005f, 0));
      } if (previousMusic.getVolume() <= 0 && previousMusic.isPlaying()) {
        previousMusic.pause();
        //previousMusic.setPosition(0);
      }
      if (currentMusic.isPlaying() || previousMusic.getVolume() <= previousVolume * 0.35f) {
        if (currentMusic.isPlaying()) {
          currentMusic.setVolume( Math.max((currentMusic.getVolume() * 190 + currentVolume) / 191f, 0) );
        } else {
          Gdx.app.postRunnable(currentMusic::play);
        }
      }
      if (currentMusic.getVolume() >= currentVolume * 0.999f - 0.0001f) {
        currentMusic.setVolume(currentVolume);
      }
    }

    @Override
    public void interrupt() {
      playerActive.set(false);
      super.interrupt();
    }
  }
}
