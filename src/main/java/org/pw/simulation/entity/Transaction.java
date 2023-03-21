package org.pw.simulation.entity;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Transaction {
  private String from;
  private String to;
  private Long timestamp;
  private int amount;
  private String signature;

  @Override
  public String toString() {
    return String.format("[%s] %s pays %s %d euro-sponges",
        new Date(timestamp).toString(), from, to, amount);
  }

}
