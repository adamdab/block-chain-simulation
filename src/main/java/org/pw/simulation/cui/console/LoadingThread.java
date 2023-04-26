package org.pw.simulation.cui.console;

public class LoadingThread extends Thread{

  private final String loadingMessage;

  public LoadingThread(String loadingMessage) {
    super();
    this.loadingMessage = loadingMessage;
  }

  @Override
  public void run() {
    char[] animation = {'|', '/','-','\\'};
    int i = 0;
    try {
      while (!Thread.currentThread().isInterrupted()) {
        Console.print(loadingMessage+"..."+animation[i]);
        i = (i+1)%4;
        Thread.sleep(30);
      }
    } catch (Exception ignore) {}
  }
}
