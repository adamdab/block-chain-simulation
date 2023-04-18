package org.pw.simulation.cui;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.pw.simulation.clients.Client;
import org.pw.simulation.entity.Transaction;
import org.pw.simulation.entity.TransactionType;
import org.pw.simulation.miners.Miner;

public class Environment {

  private final Client client;
  private final Miner miner;
  private final Console console;
  private final Parser parser;
  private final List<Transaction> allTransactions;
  private boolean quit;


  public Environment() {
    Console.showTitle();
    Console.beginning();
    miner = new Miner(List.of(), "INIT");
    console = new Console();
    parser = new Parser();
    allTransactions = new ArrayList<>();
    String username = console.askForInput("Username: ");
    client = new Client(username, List.of());
    quit = false;
  }

  public void run() {
    while (!quit) {
      execute(parser.parse(console.askForInput(client.getName()+"> ")));
    }
  }


  public void execute(Action action) {
    switch (action.getAction()) {
      case QUIT -> quit = true;
      case HELP -> help();
      case UNKNOWN_COMMAND -> unknownCommand(action);
      case BLOCK_ACTION -> invokeBlockAction(action);
      case TRANSACTION_ACTION -> invokeTransactionAction(action);
      }
    }

  private void invokeTransactionAction(Action action) {
    switch (action.getSubType()) {
      case DETAILS -> getDetailsOfTransaction(action);
      case LIST_LONG -> getLongListOfTransactions(action);
      case LIST_SHORT -> getShortListOfTransactions(action);
      case CREATE -> createTransaction(action);
      case VALIDATE -> validateTransaction(action);
      case CREATE_INVALID -> createInvalidTransaction(action);
      case BAD_REQUEST -> unknownCommand(action);
  }

}

  private void createInvalidTransaction(Action action) {
    List<String> args = action.getArgs();
    if(args.size()!=2) {
      Console.printLine("Incorrect arguments, use : [to amount]");
      return;
    }
    Long timestamp = new Date().getTime();
    int amount = Integer.parseInt(args.get(1));
    Transaction transaction = Transaction.builder()
        .from(client.getName())
        .to(args.get(0))
        .timestamp(timestamp)
        .type(TransactionType.USER_TRANSACTION)
        .amount(amount)
        .signature(client.sign((new Date(123123123L)).toString().getBytes(StandardCharsets.UTF_8)))
        .build();
    allTransactions.add(transaction);
    Console.printLine("[INFO] Transaction created successfully : " + transaction.toString());
  }

  private void validateTransaction(Action action) {
  }

  private void createTransaction(Action action) {
  }

  private void getShortListOfTransactions(Action action) {

  }

  private void getLongListOfTransactions(Action action) {

  }

  private void getDetailsOfTransaction(Action action) {

  }

  private void invokeBlockAction(Action action) {
    switch (action.getSubType()) {
      case DETAILS -> getDetailsOfBlock(action);
      case LIST_LONG -> getLongListOfBlocks(action);
      case LIST_SHORT -> getShortListOfBlocks(action);
      case CREATE -> createBlock(action);
      case VALIDATE -> validateBlock(action);
      case CREATE_INVALID -> createInvalidBlock(action);
      case BAD_REQUEST -> unknownCommand(action);
    }
  }

  private void createInvalidBlock(Action action) {
    
  }

  private void validateBlock(Action action) {
  }

  private void createBlock(Action action) {
  }

  private void getShortListOfBlocks(Action action) {

  }

  private void getLongListOfBlocks(Action action) {

  }

  private void getDetailsOfBlock(Action action) {

  }

  private void unknownCommand(Action action) {
    Console.printLine("###################################");
    Console.printLine("# An unknown command has occurred #");
    Console.printLine("# Please use /h or /help to see   #");
    Console.printLine("# available commands              #");
    Console.printLine("###################################");
  }

  private void help() {
    Console.printLine("HELP:");
    Console.printLine("[/quit /q] -> quit execution of program");
    Console.printLine("[/help /h] -> see help page");
    Console.printLine("[/block /b] [--create -c] [--list -ls] [--list-all -la] [-details -d] -> "
        + "in block mode [-create block : params = ...] [-list shorten version] [-list long version] [-details]");
    Console.printLine("[/transaction /t] [--create -c] [--list -ls] [--list-all -la] [-details -d] -> "
        + "in transaction mode [-create block : params = ...] [-list shorten version] [-list long version] [-details]");
  }

}
