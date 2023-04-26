package org.pw.simulation.cui.console;

import java.util.List;
import java.util.Scanner;

public final class Console {

  private final Scanner scanner;

  public Console() {
    scanner =  new Scanner(System.in);
  }

  public static void showTitle() {
    System.out.println(
        ConsoleColors.PURPLE_BRIGHT +
        """
               ___   __           __         __         _              _              __       __           \s
              / _ ) / /___  ____ / /__ ____ / /  ___ _ (_)___    ___  (_)__ _  __ __ / /___ _ / /_ ___   ____
             / _  |/ // _ \\/ __//  '_// __// _ \\/ _ `// // _ \\  (_-< / //  ' \\/ // // // _ `// __// _ \\ / __/
            /____//_/ \\___/\\__//_/\\_\\ \\__//_//_/\\_,_//_//_//_/ /___//_//_/_/_/\\_,_//_/ \\_,_/ \\__/ \\___//_/  \s
                                                                                                          \s                                                                                                                   \s"""
    + ConsoleColors.RESET);
  }

  public static void note(String message) {
    System.out.println(ConsoleColors.BLUE_BRIGHT + message);
    System.out.println(ConsoleColors.RESET);
    System.out.println();
  }

  public static void beginning(String message) {
    System.out.println(ConsoleColors.CYAN_BOLD+ message + ConsoleColors.RESET);
    System.out.println();
    System.out.println();
  }

  public static void print(String message) {
      System.out.print(message + "\r");
  }

  public static void printLine(String message) {
    System.out.println(message);
  }

  public static void info(String message) {
    System.out.println("["+ConsoleColors.GREEN_BRIGHT+"INFO"+ConsoleColors.RESET + "] "+message);
  }

  public static void error(String message) {
    System.out.println("["+ConsoleColors.RED+"ERROR"+ConsoleColors.RESET + "] " +message);
  }

  public static void warn(String message) {
    System.out.println("["+ConsoleColors.YELLOW+"WARN"+ConsoleColors.RESET + "] " +message);
  }

  public static void fatalError(String message) {
    System.out.println("["+ConsoleColors.RED_BRIGHT+"FATAL ERROR"+ConsoleColors.RESET+ "] "+message);
  }

  public static void printJSON(List<String> fields, List<Object> values) {
    if(fields.size()!=values.size()) return;
    System.out.println(" {");
    for (int i=0; i< fields.size(); i++) {
      System.out.println(
          ConsoleColors.CYAN + fields.get(i) +" : " + ConsoleColors.RESET + values.get(i) + ",");
    }
    System.out.println(" }");
  }

  public String scan() {
    return scanner.next();
  }
  public String askForInput(String message) {
    System.out.print(message + ConsoleColors.GREEN);
    String input = scanner.nextLine();
    System.out.print(ConsoleColors.RESET);
    return input;
  }
}
