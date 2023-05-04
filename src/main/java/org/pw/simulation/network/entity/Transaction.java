package org.pw.simulation.network.entity;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Transaction {
  private TransactionType type;
  private String from;
  private String to;
  private Long timestamp;
  private int amount;

  private byte[] signature;

  @Override
  public String toString() {
    return String.format("[%s] %s pays %s %d euro-sponges",
        new Date(timestamp).toString(), from, to, amount);
  }

}
