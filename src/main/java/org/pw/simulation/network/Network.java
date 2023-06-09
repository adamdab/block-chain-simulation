package org.pw.simulation.network;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.management.openmbean.KeyAlreadyExistsException;
import org.pw.simulation.cui.console.Console;
import org.pw.simulation.cui.console.LoadingThread;
import org.pw.simulation.cui.languages.TextProvider;
import org.pw.simulation.network.entity.Block;
import org.pw.simulation.network.entity.BlockWrapper;
import org.pw.simulation.network.entity.Transaction;
import org.pw.simulation.network.entity.TransactionType;
import org.pw.simulation.network.clients.Client;
import org.pw.simulation.network.miners.Miner;

public class Network {
  private String currentClient;
  private final TextProvider textProvider;
  private final HashMap<String, Client> clients;
  private final List<Miner> miners;

  public Network(String startingClient, TextProvider textProvider) {
    this.textProvider = textProvider;
    currentClient = startingClient;
    clients = new HashMap<>();
    miners = new ArrayList<>();
    clients.put(startingClient, new Client(startingClient, new ArrayList<>()));
    miners.add(new Miner(new ArrayList<>(), "INIT"));
  }

  public void addClient(String clientName) {
    if(clients.containsKey(clientName)) throw new KeyAlreadyExistsException();
    clients.put(clientName, new Client(clientName, new ArrayList<>(clients.get(currentClient).getChain())));
    Console.printLine(textProvider.listClients(clients.values()
        .stream()
        .map(Client::getName)
        .collect(Collectors.toList())));
  }

  public Client getCurrentClient() {
    return clients.get(currentClient);
  }

  public void changeClient(String clientName) {
    if(! clients.containsKey(clientName)) throw new KeyAlreadyExistsException();
    currentClient = clientName;
  }
  public void addMiner() {
    miners.add(new Miner(new ArrayList<>(miners.get(0).getChain()),miners.get(0).getPrevHash()));
    Console.printLine(textProvider.listMiners(miners.size()));
  }



  public void mineBlock(Transaction transaction) {
    // validate
    LoadingThread validatingThread = new LoadingThread(textProvider.validating());
    validatingThread.start();
    List<Miner> acceptingMiners = new ArrayList<>();
    for(int i = 0; i< miners.size(); i++) {
      if(miners.get(i).validate(transaction, clients.get(transaction.getFrom()).getPublicKey())) {
        Console.info(
            i + ". miner : " + textProvider.transactionName(transaction) +
                textProvider.validation(true));
        acceptingMiners.add(miners.get(i));
      } else {
        Console.warn(
            i + ". miner : " + textProvider.transactionName(transaction) +
                textProvider.validation(false));
      }
    }
    validatingThread.interrupt();
    try {
      validatingThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    Console.printLine(textProvider.endOfProcess());
    // mine
    if(acceptingMiners.isEmpty()) {
      Console.printLine(textProvider.endOfProcess());
      return;
    }
    Long now = new Date().getTime();
    LoadingThread miningThread = new LoadingThread("Mining");
    miningThread.start();
    BlockWrapper wrappedBlock = acceptingMiners.parallelStream()
        .map(miner -> {
          Block minedBlock = miner.mineBlock(now, transaction, 4);
          return BlockWrapper.builder()
              .author(miner)
              .miningDate(new Date().getTime())
              .block(minedBlock)
              .build();
        }).min((o1, o2) -> o1.getMiningDate() > o2.getMiningDate() ? 1 : 0)
        .orElseThrow();
    miningThread.interrupt();
    try {
      miningThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    Console.printLine("Mining " + textProvider.endOfProcess());

    // choose the fastest miner and get his block

    Console.info(textProvider.blockName(wrappedBlock.getBlock()));

    // broadcast to all clients

    Console.printLine("Broadcasting ...");
    for(Client client : clients.values()) {
      client.getChain().add(wrappedBlock.getBlock());
    }
    Console.printLine(textProvider.endOfProcess());
  }

  public void validateBlock(Block block) {
    for(int i=0; i< miners.size(); i++) {
      if(miners.get(i).validate(block.getTransaction(), clients.get(block.getTransaction().getFrom()).getPublicKey())) {
        if(miners.get(i).validateBlock(block)) {
          Console.info(
              i + ". miner : " + textProvider.blockName(block)
                  + textProvider.validation(true)
          );
        } else {
          Console.error(
              i + ". miner : " + textProvider.blockName(block)
                  + textProvider.validation(false)
          );
        }
      } else {
        Console.warn(
            i + ". miner : " + textProvider.transactionName(block.getTransaction())
                + textProvider.validation(false)
        );
      }
    }
  }

  public void validateTransaction(Transaction transaction) {
    for (int i = 0; i < miners.size(); i++) {
      if(miners.get(i).validate(transaction,
          clients.get(transaction.getFrom()).getPublicKey())) {
        Console.info(
            i + ". miner : " + textProvider.transactionName(transaction) +
                textProvider.validation(true));
      }
      else {
        Console.warn(
            i + ". miner : " + textProvider.transactionName(transaction)
                + textProvider.validation(false)
        );
      }
    }
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
}
