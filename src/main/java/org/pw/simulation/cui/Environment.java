package org.pw.simulation.cui;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.pw.simulation.cui.console.LoadingThread;
import org.pw.simulation.cui.languages.TextProvider;
import org.pw.simulation.cui.languages.en.EnglishTextProvider;
import org.pw.simulation.cui.languages.pl.PolishTextProvider;
import org.pw.simulation.network.clients.Client;
import org.pw.simulation.cui.actions.Action;
import org.pw.simulation.cui.console.Console;
import org.pw.simulation.entity.Block;
import org.pw.simulation.entity.Transaction;
import org.pw.simulation.entity.TransactionType;
import org.pw.simulation.network.miners.Miner;

public class Environment {

  private final Client client;
  private final Miner miner;
  private final Console console;
  private final Parser parser;
  private final List<Transaction> allTransactions;
  private final TextProvider textProvider;
  private boolean quit;


  public Environment() {
    Console.showTitle();
    miner = new Miner(new ArrayList<>(), "INIT");
    console = new Console();
    parser = new Parser();
    allTransactions = new ArrayList<>();
    String language = console.askForInput("Language PL or EN : ");
    if(language.toLowerCase().equals("en")) textProvider = new EnglishTextProvider();
    else textProvider = new PolishTextProvider();
    Console.note(textProvider.note());
    Console.beginning(textProvider.bar());
    String username = console.askForInput(textProvider.askForClientsName());
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
      Console.error(textProvider.incorrectArgumentsUsage(List.of("[to amount]")));
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
    Console.info(textProvider.invalidTransactionCreationSuccessful() + textProvider.transactionName(transaction));
  }

  private void validateTransaction(Action action) {
    List<String> args = action.getArgs();
    try {
      int index = Integer.parseInt(args.get(0));
      Transaction transaction = allTransactions.get(index);
      simulateLatency("Broadcasting",5L,120);
      String validation = textProvider.validation(miner.validate(transaction, client.getPublicKey()));
      Console.info(textProvider.transactionName(transaction) + validation);
    } catch (Exception e) {
      Console.error(textProvider.incorrectArgumentsUsage(List.of("[index]")));
    }
  }

  private void createTransaction(Action action) {
    List<String> args = action.getArgs();
    if(args.size()!=2) {
      Console.error(textProvider.incorrectArgumentsUsage(List.of("[to amount]")));
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
    Console.info(textProvider.validTransactionCreationSuccessful() + textProvider.transactionName(transaction));
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
        Console.printLine(index+". " + textProvider.shortPaymentTransaction(transaction));
      } catch (Exception e) {
        Console.error(textProvider.incorrectArgumentsUsage(List.of("[]", "[index]")));
      }
    }
  }

  private void getLongListOfTransactions(Action action) {
    if (action.getArgs().size() == 0) {
      for (int i = 0; i < allTransactions.size(); i++) {
        Transaction transaction = allTransactions.get(i);
        Console.printJSON(List.of("index", "from", "to", "amount", "timestamp", "signature"),
            List.of(i, transaction.getFrom(), transaction.getTo(),
                transaction.getAmount() + " euro-sponges", transaction.getTimestamp(),
                transaction.getSignature()));
      }
    } else {
      try {
        int index = Integer.parseInt(action.getArgs().get(0));
        Transaction transaction = allTransactions.get(index);
        Console.printJSON(List.of("index", "from", "to", "amount", "timestamp", "signature"),
            List.of(index, transaction.getFrom(), transaction.getTo(),
                transaction.getAmount() + " euro-sponges", transaction.getTimestamp(),
                transaction.getSignature()));
      } catch (Exception e) {
        Console.error(textProvider.incorrectArgumentsUsage(List.of("[]", "[index]")));
      }
    }
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
      Console.info(textProvider.validating());
      simulateLatency("Broadcasting",5L,120);
      boolean isValid = miner.validate(transaction, client.getPublicKey());
      if(!isValid) {
        Console.error("Transaction " + textProvider.validation(false));
        return;
      } else Console.info("Transaction " + textProvider.validation(true));
      LoadingThread thread = new LoadingThread("Mining block");
      thread.start();
      Block block = miner.mineBlock(new Date().getTime(), transaction, 4);
      thread.interrupt();
      thread.join();
      if(invalidate) block.invalidateNonce();
      Console.info(textProvider.validating());
      if(miner.validateBlock(block)) {
        Console.info(textProvider.blockName(block) + textProvider.validation(true));
        simulateLatency("Adding block",5L,80);
        client.addBlock(block);
      } else {
        Console.warn(textProvider.blockName(block) + textProvider.validation(false));
      }

    } catch (Exception e) {
      Console.error(textProvider.incorrectArgumentsUsage(List.of("[transaction_index]")));
    }
  }


  private void getShortListOfBlocks(Action action) {
    if(!action.getArgs().isEmpty()) {
      try {
       int index = Integer.parseInt(action.getArgs().get(0));
       Block block = client.getChain().get(index);
        Console.printLine(index + ". " + textProvider.shortBlockTransactionMined(block));
      } catch (Exception e) {
        Console.error(textProvider.incorrectArgumentsUsage(List.of("[block_index]")));
      }
    } else {
      for(int i=0;i<client.getChain().size(); i++) {
        Block block = client.getChain().get(i);
        Console.printLine(i + ". " + textProvider.shortBlockTransactionMined(block));
      }
    }
  }

  private void getLongListOfBlocks(Action action) {
    try {
    if(action.getArgs().isEmpty()) {
        for(int i=0;i<client.getChain().size(); i++) {
          Block block = client.getChain().get(i);
          Console.printJSON(
              List.of("index", "transaction", "mined at", "nonce", "prev hash", "hash"),
              List.of(i, block.getTransaction().toString(), block.getTimeStamp(), block.getNonce(),
                  block.getPreviousHash(), block.getHash()));
        }
      } else {
          int index = Integer.parseInt(action.getArgs().get(0));
          Block block = client.getChain().get(index);
          Console.printJSON(List.of("index","transaction","mined at","nonce","prev hash","hash"),
              List.of(index, block.getTransaction().toString(), block.getTimeStamp(), block.getNonce(),
                  block.getPreviousHash(), block.getHash()));
        }
      }  catch (Exception e) {
        Console.error(textProvider.incorrectArgumentsUsage(List.of("[block_index]")));
    }
  }


  private void unknownCommand(Action action) {
    Console.printLine(textProvider.unknownCommand());
  }

  private void help() {
    Console.printLine(textProvider.help());
  }

  private void clear(){
    System.out.print("\033[H\033[2J");
    System.out.flush();
    Console.showTitle();
    Console.beginning(textProvider.bar());
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
    Console.print(message + textProvider.endOfProcess()+"\n");
  }

}
