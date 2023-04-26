package org.pw.simulation.network;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.management.openmbean.KeyAlreadyExistsException;
import org.pw.simulation.cui.console.Console;
import org.pw.simulation.entity.Block;
import org.pw.simulation.entity.Transaction;
import org.pw.simulation.entity.TransactionType;
import org.pw.simulation.network.clients.Client;
import org.pw.simulation.network.miners.Miner;

public class Network {
  public String currentClient;
  private final HashMap<String, Client> clients;
  private final List<Miner> miners;

  public Network(String startingClient) {
    currentClient = startingClient;
    clients = new HashMap<>();
    miners = new ArrayList<>();
    clients.put(startingClient, new Client(startingClient, new ArrayList<>()));
    miners.add(new Miner(new ArrayList<>(), "INIT"));
  }

  public void addClient(String clientName) {
    if(clients.containsKey(clientName)) throw new KeyAlreadyExistsException();
    clients.put(clientName, new Client(clientName, clients.get(currentClient).getChain()));
  }

  public void addMiner() {
    miners.add(new Miner(miners.get(0).getChain(),miners.get(0).getPrevHash()));
  }

  public void changeClient(String clientName) {
    if(! clients.containsKey(clientName)) throw new ArrayStoreException();
    currentClient = clientName;
  }


  public void mineBlock(Transaction transaction) {
    Block block = miners.parallelStream()
        .map(miners -> miners.mineBlock(new Date().getTime(), transaction, 0))
        .min(Comparator.comparing(Block::getTimeStamp))
        .orElseThrow();
  }

  public Transaction createValidTransaction(String to, int amount) {
    Long timestamp = new Date().getTime();
    return Transaction.builder()
        .type(TransactionType.USER_TRANSACTION)
        .amount(amount)
        .from(currentClient)
        .to(to)
        .timestamp(timestamp)
        .signature(clients.get(currentClient).sign(timestamp.toString().getBytes(StandardCharsets.UTF_8)))
        .build();
  }

  public Transaction createInvalidTransaction(String to, int amount) {
    Long timestamp = new Date().getTime();
    Long wrongTimestamp = timestamp - 100000L;
    return Transaction.builder()
        .type(TransactionType.USER_TRANSACTION)
        .amount(amount)
        .from(currentClient)
        .to(to)
        .timestamp(timestamp)
        .signature(clients.get(currentClient).sign(wrongTimestamp.toString().getBytes(StandardCharsets.UTF_8)))
        .build();
  }

  public void broadcastTransaction(Transaction transaction) {
    // broadcasts and validates transaction !does not start process of adding transaction to chain
    for(int i=0; i<miners.size(); i++) {
      if(miners.get(i).validate(transaction, clients.get(transaction.getFrom()).getPublicKey())) {
        Console.info("Miner : " + i + " validated transaction as VALID");
      }
      else Console.warn("Miner : " + i + " validated transaction as INVALID");
    }
  }

  public void broadcastBlock(Block block) {

  }
}
