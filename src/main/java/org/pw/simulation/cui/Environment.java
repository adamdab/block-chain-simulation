package org.pw.simulation.cui;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.pw.simulation.clients.Client;
import org.pw.simulation.cui.actions.Action;
import org.pw.simulation.cui.console.Console;
import org.pw.simulation.entity.Block;
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
    miner = new Miner(new ArrayList<>(), "INIT");
    console = new Console();
    parser = new Parser();
    allTransactions = new ArrayList<>();
    String username = console.askForInput("Client's name (You) : ");
    client = new Client(username, new ArrayList());
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
      case CLEAR -> clear();
      case HELP -> help();
      case WHITESPACE -> Console.print("");
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
    Console.printLine("[INFO] Invalid transaction created successfully : " + transaction.toString());
  }

  private void validateTransaction(Action action) {
    List<String> args = action.getArgs();
    try {
      int index = Integer.parseInt(args.get(0));
      Transaction transaction = allTransactions.get(index);
      simulateLatency("Broadcasting",5L,120);
      String validation = miner.validate(transaction, client.getPublicKey())? " is VALID" : " is INVALID";
      Console.printLine("[INFO] Transaction : " + transaction.toString() + validation);
    } catch (Exception e) {
      Console.printLine("Incorrect arguments, use : [index]");
    }
  }

  private void createTransaction(Action action) {
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
        .signature(client.sign(timestamp.toString().getBytes(StandardCharsets.UTF_8)))
        .build();
    allTransactions.add(transaction);
    Console.printLine("[INFO] Valid transaction created successfully : " + transaction.toString());
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
      case CREATE_INVALID -> createInvalidBlock(action);
      case BAD_REQUEST -> unknownCommand(action);
    }
  }

  private void createInvalidBlock(Action action) {
    mineBlock(action,true);
  }

  private void createBlock(Action action) {
    mineBlock(action, false);
  }

  private void mineBlock(Action action, boolean invalidate) {
    try {
      int index = Integer.parseInt(action.getArgs().get(0));
      Transaction transaction = allTransactions.get(index);
      Console.printLine("[INFO] Validating transaction...");
      simulateLatency("Broadcasting",5L,120);
      boolean isValid = miner.validate(transaction, client.getPublicKey());
      if(!isValid) {
        Console.printLine("[ERROR] Transaction is INVALID");
        return;
      } else Console.printLine("[INFO] Transaction is VALID");
      Block block = miner.mineBlock(new Date().getTime(), transaction, 4);
      if(invalidate) block.invalidateNonce();
      Console.printLine("[INFO] Validating block...");
      if(miner.validateBlock(block)) {
        Console.printLine("[INFO] Block : " + block.toString() + "is VALID");
        simulateLatency("Adding block",5L,80);
        client.addBlock(block);
      } else {
        Console.printLine("[WARN] Block is INVALID");
      }

    } catch (Exception e) {
      Console.printLine("Incorrect arguments, use [transaction_index]");
    }
  }


  private void getShortListOfBlocks(Action action) {
    if(!action.getArgs().isEmpty()) {
      try {
       int index = Integer.parseInt(action.getArgs().get(0));
       Block block = client.getChain().get(index);
        Console.printLine(index + ". Transaction : " + block.getTransaction().toString() + ", mined at : " + block.getTimeStamp());
      } catch (Exception e) {
        Console.printLine("Incorrect arguments, use [block_index]");
      }
    } else {
      for(int i=0;i<client.getChain().size(); i++) {
        Block block = client.getChain().get(i);
        Console.printLine(i + ". Transaction : " + block.getTransaction().toString() + ", mined at : " + block.getTimeStamp());
      }
    }

  }

  private void getLongListOfBlocks(Action action) {
    if(action.getArgs().isEmpty()) {
      try {
        for(int i=0;i<client.getChain().size(); i++) {
          Block block = client.getChain().get(i);
          Console.printLine(
              i + ". {Transaction : " + block.getTransaction().toString() + ",\nmined at : "
                  + block.getTimeStamp() + ",\nnonce : " + block.getNonce() + ",\nprev hash : "
                  + block.getPreviousHash() + "\nhash : " + block.getHash() + " }");
        }
      } catch (Exception e) {
        Console.printLine("Incorrect arguments, use [block_index]");
      }
    }
  }


  private void unknownCommand(Action action) {
    Console.printLine("#---------------------------------#");
    Console.printLine("| An unknown command has occurred |");
    Console.printLine("| Please use /h or /help to see   |");
    Console.printLine("| available commands              |");
    Console.printLine("#---------------------------------#");
  }

  private void help() {
    Console.printLine("""
          HELP : /help or /h
            DESC : returns list of commands that user can invoke
            
          QUIT : /quit or /q
            DESC : closes the application
            
          CLEAR : /clear or /c
            DESC : clear the console
          
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
              PARAMETERS : [transaction_index]
              DESC : create valid block and add it to the blockchain
              
             --create-invalid or -ci
              PARAMETERS : [transaction_index]
              DESC: create invalid block and add tries to add it to the blockchain
             
             --list or -ls
              PARAMETERS : [] or [index]
              DESC : get short description of block in blockchain 
                     if no index is specified it returns list of all transactions
                     
             --list-all or -la
              PARAMETERS : [] or [index]
              DESC : get long description of block in blockchain  
                     if no index is specified it returns list of all transactions
        """);
  }

  private void clear(){
    System.out.print("\033[H\033[2J");
    System.out.flush();
    Console.showTitle();
    Console.beginning();
  }

  private void simulateLatency(String message, long latency, int counter) {
    char[] animation = {'|', '/','-','\\'};
    try {
      for(int i = 0; i<counter; i++) {
        Console.print(message+" ... " + animation[i%4]);
        Thread.sleep(latency);
      }
    } catch (Exception ignored) {
    }
    Console.print(message + " ... Done !\n");
  }

}
