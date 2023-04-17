package org.pw.simulation.cui;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Action {
  public ActionType action;
  public ActionSubType subType;
  public List<String> args;
}
