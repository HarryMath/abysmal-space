package com.mikilangelo.abysmal.screens.menu;

public class SettingOption extends Option {

  private boolean flag;

  public SettingOption(String text, boolean flag, Runnable action) {
    super(text, action);
    this.flag = flag;
  }

  public String getText() {
    return text + "  [" + (flag ? "yes" : "no") + "]";
  }

  public void click() {
    this.flag = !this.flag;
    action.run();
  }
}
