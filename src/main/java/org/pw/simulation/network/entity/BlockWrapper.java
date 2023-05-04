package org.pw.simulation.network.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.pw.simulation.network.miners.Miner;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BlockWrapper {
  private long miningDate;
  private Block block;
  private Miner author;
}
