package com.mikilangelo.abysmal.ui.gameElemets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;

public class Indicator extends InterfaceElement {

  private static final byte FULL_HEIGHT = 24,
          INDICATOR_START_X = 3, INDICATOR_START_Y = 19, INDICATOR_MAX_WIDTH = 54,
          ICON_START_X = 6, ICON_START_Y = 6, ICON_HEIGHT = 7,
          BIG_FONT_HEIGHT = 9, BIG_FONT_START_Y = 5,
          SMALL_FONT_HEIGHT = 5, SMALL_START_Y = 6;

  private static Texture background, border;

  public static void init() {
    background = TexturesRepository.get("UI/indicators/back.png");
    border = TexturesRepository.get("UI/indicators/border.png");
  }

  private final Texture icon;
  private final Sprite bar;
  private final int maxValue;
  private final byte maxValueLength;
  private final float r, g, b;
  private final float initX;
  private float x, iconX, barX, shownValue, smallFontSize, iconWidth, alphaModulation;
  private static float y, barHeight, barY, barMaxWidth, bigFontSize, bigFontY,
          smallFontY, iconY, iconHeight, fullHeight, fullWidth, fontScale;

  public Indicator(String iconName, String barColor, Vector3 color, BitmapFont font, int max, float x, int screenHeight) {
    icon = TexturesRepository.get("UI/indicators/" + iconName + ".png");
    bar = new Sprite(TexturesRepository.get("UI/indicators/" + barColor + ".png"));
    r = color.x; g = color.y; b = color.z;
    maxValueLength = (byte) String.valueOf(max).length();
    this.maxValue = max;
    this.shownValue = 0;
    this.initX = x;
    alphaModulation = 0;
    fontScale = font.getScaleY() / font.getCapHeight();
    handleResize(screenHeight);
    resize(screenHeight);
  }

  public void draw(Batch batch, BitmapFont font, float currentValue) {
    if (currentValue < 0) {
      currentValue = 0;
    } else if (currentValue > maxValue) {
      currentValue = maxValue;
    }
    shownValue = shownValue * 0.9f + currentValue * 0.1f;

    batch.draw(background, x, y, fullWidth, fullHeight);
    bar.setAlpha(0.25f + 0.45f * shownValue / maxValue);
    bar.draw(batch);
    batch.draw(bar, barX, barY, barMaxWidth * shownValue / maxValue, barHeight);
    batch.draw(bar, barX + barMaxWidth * shownValue / maxValue - RATIO * 0.005f,
            barY + barHeight * 0.25f, RATIO * 0.9f, barHeight * 0.5f);
    batch.draw(border, x, y, fullWidth, fullHeight);
    batch.draw(icon, iconX, iconY, iconWidth, iconHeight);

    font.getData().setScale(bigFontSize * fontScale);
    final byte length = (byte) String.valueOf((int) currentValue).length();
    final String zeros = "0000".substring(0, 4 - length);
    float textX = iconX + iconWidth + 4 * RATIO;
    textX += glitchText(batch, font, zeros, textX, bigFontY, 0.15f) + 2 * RATIO;
    textX += glitchText(batch, font, String.valueOf((int) currentValue), textX, bigFontY, 1) + 2 * RATIO;
    float smallFontScale = (x + fullWidth - textX) / (16 * RATIO) * 2 / maxValueLength;
    smallFontScale = smallFontScale >= 1 ? 1 : (smallFontScale + smallFontScale * 16 / 12 * 2) / 3;
    font.getData().setScale(smallFontScale * smallFontSize * fontScale);
    glitchText(batch, font, "|" + maxValue, textX, smallFontY, 0.6f);
  }

  /*
  * return width of drawn text
  * */
  private float glitchText(Batch batch, BitmapFont font, String text, float x, float y, float alpha) {
    alphaModulation = (alphaModulation * 0.9f + (MathUtils.random() > 0.5f ? 0.7f : 0.2f) * 0.1f);
    font.setColor(0.6f + 0.4f * r, 0, 0.3f + 0.3f * b, alpha * alphaModulation);
    font.draw(batch, text, x - RATIO * 0.25f, y + RATIO * 0.5f);
    font.setColor(0, 0.6f + 0.4f * g, 0.3f + 0.3f * b, alpha * alphaModulation);
    font.draw(batch, text, x + RATIO * 0.25f, y - RATIO * 0.5f);
    font.setColor(r, g, b, alpha * (0.8f + 0.2f * alphaModulation));
    return font.draw(batch, text, x, y).width;
  }

  public static void handleResize(int h) {
    InterfaceElement.handleResize(h);
    fullHeight = FULL_HEIGHT * RATIO;
    fullWidth = fullHeight * background.getWidth() / background.getHeight();
  }

  public void resize(int h) {
    x = initX * RATIO;
    y = h - FULL_HEIGHT * RATIO - 4 * RATIO;
    barHeight = 3.1f * RATIO;
    barX = x + INDICATOR_START_X * RATIO;
    barY = y + fullHeight - INDICATOR_START_Y * RATIO - barHeight;
    barMaxWidth = INDICATOR_MAX_WIDTH * RATIO;
    bigFontSize = BIG_FONT_HEIGHT * RATIO;
    bigFontY = y + fullHeight - BIG_FONT_START_Y * RATIO;
    smallFontSize = SMALL_FONT_HEIGHT * RATIO;
    smallFontY = y + fullHeight - SMALL_START_Y * RATIO;
    iconHeight = ICON_HEIGHT * RATIO;
    iconX = x + ICON_START_X * RATIO;
    iconY = y + fullHeight - ICON_START_Y * RATIO - iconHeight;
    iconWidth = iconHeight * icon.getWidth() / icon.getHeight();
    bar.setCenter(barX + barMaxWidth / 2, barY + 2 * RATIO);
    bar.setScale(barMaxWidth + RATIO, 2.2f * RATIO);
  }
}
