package org.pw.simulation.miners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pw.simulation.network.clients.Client;
import org.pw.simulation.network.entity.Block;
import org.pw.simulation.network.entity.Transaction;
import org.pw.simulation.network.miners.Miner;

class MinerTest {
  @Test
  void shouldCorrectlyCreateHashTest() {
    int prefix = 4;
    String prefixString = new String(new char[prefix]).replace('\0', '0');
    Long now = new Date().getTime();
    Transaction transaction = Transaction.builder()
        .timestamp(now)
        .build();
    Miner miner = new Miner(new ArrayList<>(), "INIT");
    Block block = miner.mineBlock(new Date().getTime(), transaction, prefix);
    System.out.println(block);
    assertEquals(prefixString, block.getHash().substring(0, prefix));
    assertEquals(block.getHash(), miner.getPrevHash());
    assertTrue(miner.validateBlock(block));
  }

  @Test
  void shouldCorrectlyValidateTransactionTest() {
    Miner miner = new Miner(new ArrayList<>(), "INIT");
    Client client = new Client("Test Client", List.of());
    Long now = new Date().getTime();
    Transaction transaction = Transaction.builder()
        .timestamp(now)
        .signature(client.sign(now.toString().getBytes(StandardCharsets.UTF_8)))
        .build();
    assertTrue(miner.validate(transaction, client.getPublicKey()));
  }

  @Test
  void shouldCorrectlyValidateInvalidTransactionTest() {
    Miner miner = new Miner(new ArrayList<>(), "INIT");
    Client client = new Client("Test Client", List.of());
    Long now = new Date().getTime();
    Transaction transaction = Transaction.builder()
        .timestamp(now)
        .signature(client.sign((new Date(123123123L)).toString().getBytes(StandardCharsets.UTF_8)))
        .build();
    assertFalse(miner.validate(transaction, client.getPublicKey()));
  }

}