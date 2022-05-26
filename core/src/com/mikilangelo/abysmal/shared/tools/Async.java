package com.mikilangelo.abysmal.shared.tools;

public class Async extends Thread {

  private Runnable afterSuccess;
  private boolean hasThenMethod;

  public Async(Runnable action) {
    super(action);
  }

  @Override
  public void run() {
    Logger.log(this, "run", "started");
    super.run();
    Logger.log(this, "run", "ended");
    if (hasThenMethod) {
      Logger.log(this, "run", "afterSuccess started");
      afterSuccess.run();
    }
  }

  public Async then(Runnable action) {
    this.hasThenMethod = true;
    this.afterSuccess = action;
    return this;
  }
}
