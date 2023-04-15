package org.pw.simulation;

import org.pw.simulation.cui.Display;

public class Main {

  private static final Display display = new Display();
  public static void main(String[] args) {
    display.showTitle();
    display.showInSameLine("loading environment",100);
  }
}