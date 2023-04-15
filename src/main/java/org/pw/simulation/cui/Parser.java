package org.pw.simulation.cui;

import java.util.List;
import java.util.Scanner;

public class Parser {
  private final Scanner scanner;

  public Parser() {
    this.scanner = new Scanner(System.in);
  }

  public String scan() {
    return scanner.next();
  }

  public List<Action> parse(String input) {

    return List.of();
  }


}
