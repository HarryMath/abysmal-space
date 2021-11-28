package com.mikilangelo.abysmal.models.game.animations;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

public class BlackHole {

  public final float x;
  public final float y;
  private final float layer;
  private float angle;
  private final float aspectRatio;
  private float size;

  public BlackHole(float x, float y, float layer) {
    this.x = x;
    this.y = y;
    this.layer = layer;
    this.angle = 0;
    this.aspectRatio = SCREEN_HEIGHT / SCREEN_WIDTH;
    this.size = 0.00001f;
  }

  public void setUpShader(ShaderProgram shader, float playerX, float playerY, float zoom) {
    shader.setUniformf("zoom", zoom);
    shader.setUniformf("aspectRatio", getAspectRatio());
    shader.setUniformf("r", getSize());
    shader.setUniformf("angle", getAngle());
    shader.setUniformf("center", getScreenPosition(playerX, playerY, zoom));

  }

  private Vector2 getScreenPosition(float playerX, float playerY, float zoom) {
    angle += 0.01f;
    return new Vector2(
            0.5f + (x + (playerX - x) * layer - playerX) * (float) Math.pow(zoom, layer) / SCREEN_WIDTH / zoom,
            0.5f + (y + (playerY - y) * layer - playerY) * (float) Math.pow(zoom, layer) / SCREEN_HEIGHT / zoom
    );
  }

  private float getAngle() {
    return angle;
  }

  private float getAspectRatio() {
    return aspectRatio;
  }

  private float getSize() {
    if (size < 0.029f) {
      size = (size * 350 + 0.029f) / 351f;
    }
    return size;
  }
}
