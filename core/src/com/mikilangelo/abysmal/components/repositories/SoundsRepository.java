package com.mikilangelo.abysmal.components.repositories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public abstract class SoundsRepository {

  private static final Array<Music> tracks = new Array<>();
  private static final Array<Sound> sounds = new Array<>();
  private static final Map<String, Integer> tracksPaths = new HashMap<>();
  private static final Map<String, Integer> soundsPaths = new HashMap<>();

  private static void loadTracks(String prefix, Array<String> pathsList) {
    for (String path: pathsList) {
      tracksPaths.put(prefix + path, tracks.size);
      tracks.add(Gdx.audio.newMusic(Gdx.files.internal(prefix + path)));
    }
  }

  private static void loadSounds(String prefix, Array<String> pathsList, String postfix) {
    for (String path: pathsList) {
      soundsPaths.put(prefix + path + postfix, sounds.size);
      sounds.add(Gdx.audio.newSound(Gdx.files.internal(prefix + path + postfix)));
    }
  }

  private static void loadSounds(String prefix, String[] pathsList, String postfix) {
    loadSounds(prefix, new Array<>(pathsList), postfix);
  }

  private static void loadTracks(String prefix, String[] pathsList) {
    loadTracks(prefix, new Array<>(pathsList));
  }

  public static void init() {
    loadTracks("sounds/", new String[]{
            "menu.mp3", "music.mp3", "battle2.mp3", "radar.mp3"});
    loadSounds("", new String[]{
            "explosions/simple/sound.mp3", "explosions/stone/sound.mp3", "sounds/shieldHit.wav"}, "");
    loadSounds("ships/", new String[]{"alien", "defender", "hyperion", "invader"}, "/shot.mp3");
  }

  public static Music getMusic(String path) {
    return tracks.get(tracksPaths.get(path));
  }

  public static Sound getSound(String path) {
    return sounds.get(soundsPaths.get(path));
  }

  public static void disposeAll() {
    for (Music t: tracks) {
      t.dispose();
    }
    for (Sound s: sounds) {
      s.dispose();
    }
    sounds.clear();
    soundsPaths.clear();
    tracks.clear();
    tracksPaths.clear();
  }
}
