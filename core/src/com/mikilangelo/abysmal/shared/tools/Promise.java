package com.mikilangelo.abysmal.shared.tools;

public class Promise extends Thread {

  private Runnable afterSuccess;
  private boolean hasThenMethod;

  public Promise(Runnable action) {
    super(action);
  }

  @Override
  public void run() {
    super.run();
    if (hasThenMethod) {
      afterSuccess.run();
    }
  }

  public void then(Runnable action) {
    this.hasThenMethod = true;
    this.afterSuccess = action;
  }
}
