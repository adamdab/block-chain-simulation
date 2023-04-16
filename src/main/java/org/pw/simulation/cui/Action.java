package org.pw.simulation.cui;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Action {
  public ActionType action;
  public List<String> args;
}
