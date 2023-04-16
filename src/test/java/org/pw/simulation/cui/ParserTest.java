package org.pw.simulation.cui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ParserTest {

  private final Parser parser = new Parser();

  @Test
  public void shouldParseCorrectly() {
    String input = "/h command with args";
    Action action = parser.parse(input);
    assertEquals(action.getAction(), ActionType.HELP);
    assertEquals(action.getArgs(), List.of("command", "with", "args"));
  }

}