package org.pw.simulation.cui;

public class Display {

  public void showTitle() {
    System.out.println(
        """
               ___   __           __         __         _              _              __       __           \s
              / _ ) / /___  ____ / /__ ____ / /  ___ _ (_)___    ___  (_)__ _  __ __ / /___ _ / /_ ___   ____
             / _  |/ // _ \\/ __//  '_// __// _ \\/ _ `// // _ \\  (_-< / //  ' \\/ // // // _ `// __// _ \\ / __/
            /____//_/ \\___/\\__//_/\\_\\ \\__//_//_/\\_,_//_//_//_/ /___//_//_/_/_/\\_,_//_/ \\_,_/ \\__/ \\___//_/  \s
                                                                                                          \s                                                                                                                   \s"""
    );
  }

  public void showInSameLine(String message, int counter) {
    for(int i=0;i< counter;i++) {
      System.out.println(message+"..."+i+"\r");
    }
  }

}
