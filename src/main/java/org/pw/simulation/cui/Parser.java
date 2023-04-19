package org.pw.simulation.cui;

import java.util.Arrays;
import java.util.List;
import org.pw.simulation.cui.actions.Action;
import org.pw.simulation.cui.actions.ActionSubType;
import org.pw.simulation.cui.actions.ActionType;

public class Parser {

  public Action parse(String input) {
    if(input.isEmpty()) return new Action(ActionType.WHITESPACE, null, List.of());
    if(input.charAt(0)!='/') return new Action(ActionType.UNKNOWN_COMMAND, null ,List.of(input));
    List<String> request = Arrays.stream(input.split("\\s")).toList();
    return getAction(request);
  }

  private Action getAction(List<String> request) {
    if(request.size() > 2 && request.get(1).charAt(0)!='-') return new Action(ActionType.UNKNOWN_COMMAND, ActionSubType.BAD_REQUEST, request);
    String action = request.get(0);
    ActionType actionType = getActionTypeFromString(action);
    if(request.size() > 1) {
      String subAction = request.get(1);
      List<String> args = request.subList(2, request.size());
      ActionSubType subType = getSubType(subAction);
      return new Action(actionType, subType, args);
    }
    return new Action(actionType, ActionSubType.BAD_REQUEST, List.of());
  }

  private ActionSubType getSubType(String subAction) {
    return switch (subAction) {
      case "--create", "-c" -> ActionSubType.CREATE;
      case "--list", "-ls" -> ActionSubType.LIST_SHORT;
      case "--list-all", "-la" -> ActionSubType.LIST_LONG;
      case "--validate", "-v" -> ActionSubType.VALIDATE;
      case "--create-invalid", "-ci" -> ActionSubType.CREATE_INVALID;
      default -> ActionSubType.BAD_REQUEST;
    };
  }

  private ActionType getActionTypeFromString(String action) {
    if( action==null || action.isEmpty()) return ActionType.WHITESPACE;
    action = action.substring(1);
    return switch (action) {
      case "quit", "q" -> ActionType.QUIT;
      case "help", "h" -> ActionType.HELP;
      case "block", "b" -> ActionType.BLOCK_ACTION;
      case "transaction", "t" -> ActionType.TRANSACTION_ACTION;
      default -> ActionType.UNKNOWN_COMMAND;
    };
  }


}
