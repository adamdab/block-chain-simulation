package org.pw.simulation.cui;

import java.util.Arrays;
import java.util.List;

public class Parser {

  public Action parse(String input) {
    if(input.isEmpty() || input.charAt(0)!='/') return new Action(ActionType.UNKNOWN_COMMAND, List.of(input));
    List<String> request = Arrays.stream(input.split("\\s")).toList();
    String action = request.get(0);
    List<String> args = request.subList(1, request.size());
    ActionType actionType = getActionTypeFromString(action);
    return new Action(actionType, args);
  }

  private ActionType getActionTypeFromString(String action) {
    action = action.substring(1);
    return switch (action) {
      case "q", "quit" -> ActionType.QUIT;
      case "help", "h" -> ActionType.HELP;
      default -> ActionType.UNKNOWN_COMMAND;
    };
  }


}
