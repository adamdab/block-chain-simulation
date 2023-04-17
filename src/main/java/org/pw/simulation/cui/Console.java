package org.pw.simulation.cui;

import java.util.Scanner;

public final class Console {

  private final Scanner scanner;

  public Console() {
    scanner =  new Scanner(System.in);
  }

  public static void showTitle() {
    System.out.println(
        """
               ___   __           __         __         _              _              __       __           \s
              / _ ) / /___  ____ / /__ ____ / /  ___ _ (_)___    ___  (_)__ _  __ __ / /___ _ / /_ ___   ____
             / _  |/ // _ \\/ __//  '_// __// _ \\/ _ `// // _ \\  (_-< / //  ' \\/ // // // _ `// __// _ \\ / __/
            /____//_/ \\___/\\__//_/\\_\\ \\__//_//_/\\_,_//_//_//_/ /___//_//_/_/_/\\_,_//_/ \\_,_/ \\__/ \\___//_/  \s
                                                                                                          \s                                                                                                                   \s"""
    );
  }

  public static void print(String message) {
      System.out.print(message + "\r");
  }

  public static void printLine(String message) {
    System.out.println(message);
  }


  public String scan() {
    return scanner.next();
  }
  public String askForInput(String message) {
    System.out.print(message);
    return scanner.nextLine();
  }
}
