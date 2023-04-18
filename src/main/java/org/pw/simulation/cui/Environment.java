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
    if(action.getArgs().size() == 0) {
      for(int i = 0; i<allTransactions.size(); i++) {
        Console.printLine(i + ". " + allTransactions.get(i).getTo() + " : " + allTransactions.get(i).getAmount());
      }
    }
    else {
      try {
        int index = Integer.parseInt(action.getArgs().get(0));
        Transaction transaction = allTransactions.get(index);
        Console.printLine(index+". " + client.getName() + " pays "+ transaction.getAmount() + " euro-sponges to "+ transaction.getTo());
      } catch (Exception e) {
        Console.printLine("Incorrect arguments, use [] or [index]");
      }
    }
  }

  private void getLongListOfTransactions(Action action) {
    if (action.getArgs().size() == 0) {
      for (int i = 0; i < allTransactions.size(); i++) {
        Console.printLine(i + ". " + getLongDescription(allTransactions.get(i)));
      }
    } else {
      try {
        int index = Integer.parseInt(action.getArgs().get(0));
        Transaction transaction = allTransactions.get(index);
        Console.printLine(getLongDescription(transaction));
      } catch (Exception e) {
        Console.printLine("Incorrect arguments, use [] or [index]");
      }
    }
  }

  private  String getLongDescription(Transaction transaction) {
    return "{\nfrom: " + transaction.getFrom() + ",\nto: " + transaction.getTo() + ",\namount: "
        + transaction.getAmount() + " euro-sponges,\ntimestamp: " + transaction.getTimestamp()
        + ",\nsignature: " + new String(transaction.getSignature(), StandardCharsets.UTF_8) + "\n}";
  }



  private void invokeBlockAction(Action action) {
    switch (action.getSubType()) {
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


  private void unknownCommand(Action action) {
    Console.printLine("###################################");
    Console.printLine("# An unknown command has occurred #");
    Console.printLine("# Please use /h or /help to see   #");
    Console.printLine("# available commands              #");
    Console.printLine("###################################");
  }

  private void help() {
    Console.printLine("""
          HELP : /help or /h
            DESC : returns list of commands that user can invoke
            
          QUIT : /quit or /q
            DESC : closes the application
          
          TRANSACTION : /transaction or /t
            FLAGS :
             --create or -c
              PARAMETERS : [to amount]
              DESC : create valid transaction and add it to the transaction list
              
             --create-invalid or -ci
              PARAMETERS : [to amount]
              DESC: create invalid transaction and add it to the transaction list
             
             --list or -ls
              PARAMETERS : [] or [index]
              DESC : get short description of transaction that were created 
                     if no index is specified it returns list of all transactions
                     
             --list-all or -la
              PARAMETERS : [] or [index]
              DESC : get long description of transaction that were created 
                     if no index is specified it returns list of all transactions
                     
             --validate or -v
              PARAMETERS : [index]
              DESC : validates signature of transaction, 
                     this part is executing by miner before mining process
                     
              BLOCK : /block or /b
            FLAGS :
             --create or -c 
              PARAMETERS : [some parameters]
              DESC : create valid block and add it to the blockchain
              
             --create-invalid or -ci
              PARAMETERS : [some parameters]
              DESC: create invalid block and add tries to add it to the blockchain
             
             --list or -ls
              PARAMETERS : [] or [index]
              DESC : get short description of block in blockchain 
                     if no index is specified it returns list of all transactions
                     
             --list-all or -la
              PARAMETERS : [] or [index]
              DESC : get long description of block in blockchain  
                     if no index is specified it returns list of all transactions
                     
             --validate or -v
              PARAMETERS : [index]
              DESC : validates hash of block, this part is executing by
                     miners and clients before adding to blockchain
        """);
  }

}
