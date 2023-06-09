package org.pw.simulation.cui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pw.simulation.cui.actions.Action;
import org.pw.simulation.cui.actions.ActionSubType;
import org.pw.simulation.cui.actions.ActionType;

class ParserTest {

  private final Parser parser = new Parser();

  @Test
  public void shouldParseCorrectlySingleCommand() {
    String input = "/h";
    Action action = parser.parse(input);
    assertEquals(action.getAction(), ActionType.HELP);
    assertEquals(action.getSubType(), ActionSubType.BAD_REQUEST);
    assertEquals(action.getArgs(), List.of());
  }

  @Test
  public void shouldParseCorrectlyDoubleCommand() {
    String input = "/b -c transaction";
    Action action = parser.parse(input);
    assertEquals(action.getAction(), ActionType.BLOCK_ACTION);
    assertEquals(action.getSubType(), ActionSubType.CREATE);
    assertEquals(action.getArgs(), List.of("transaction"));
  }

  @Test
  public void shouldParseWhiteSpaces() {
    String input = "";
    Action action = parser.parse(input);
    assertEquals(action.getAction(), ActionType.WHITESPACE);
  }

}