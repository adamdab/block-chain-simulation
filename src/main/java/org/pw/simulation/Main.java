package org.pw.simulation;

import org.pw.simulation.cui.Environment;


public class Main {
  private static final Environment environment = new Environment();
  public static void main(String[] args) {
    environment.run();
  }
}