package org.pw.simulation.miners;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.pw.simulation.entity.Block;

class MinerTest {
  @Test
  void shouldCorrectlyCreateHash() {
    int prefix = 4;
    String prefixString = new String(new char[prefix]).replace('\0', '0');
    Miner miner = new Miner(new ArrayList<>(), "INIT");
    Block block = miner.mineBlock(new Date().getTime(),"Trnasaction #1", prefix);
    System.out.println(block);
    assertEquals(prefixString, block.getHash().substring(0, prefix));
    assertEquals(block.getHash(), miner.getPrevHash());
  }


}