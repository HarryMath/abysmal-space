package com.mikilangelo.abysmal.shared.tools;

public class Async extends Thread {

  private Runnable afterSuccess;
  private boolean hasThenMethod;

  public Async(Runnable action) {
    super(action);
  }

  @Override
  public void run() {
    System.out.println("[Async] started run");
    super.run();
    System.out.println("[Async] ended run");
    if (hasThenMethod) {
      System.out.println("[Async] afterSuccess Started");
      afterSuccess.run();
    }
  }

  public Async then(Runnable action) {
    this.hasThenMethod = true;
    this.afterSuccess = action;
    return this;
  }
}
