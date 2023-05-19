package org.pw.simulation.cui;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.management.openmbean.KeyAlreadyExistsException;
import org.pw.simulation.cui.languages.TextProvider;
import org.pw.simulation.cui.languages.en.EnglishTextProvider;
import org.pw.simulation.cui.languages.pl.BalickiTextProvider;
import org.pw.simulation.cui.languages.pl.PolishTextProvider;
import org.pw.simulation.network.Network;
import org.pw.simulation.cui.actions.Action;
import org.pw.simulation.cui.console.Console;
import org.pw.simulation.network.entity.Block;
import org.pw.simulation.network.entity.Transaction;
import org.pw.simulation.network.entity.TransactionType;

public class Environment {

  private final Console console;
  private final Parser parser;
  private final Network network;
  private final List<Transaction> allTransactions;
  private final TextProvider textProvider;
  private boolean quit;


  public Environment() {
    Console.showTitle();
    console = new Console();
    parser = new Parser();
    allTransactions = new ArrayList<>();
    String language = console.askForInput("Language PL or EN : ");
    if(language.toLowerCase().equals("en")) textProvider = new EnglishTextProvider();
    else textProvider = new PolishTextProvider();
    Console.note(textProvider.note());
    Console.beginning(textProvider.bar());
    String username = console.askForInput(textProvider.askForClientsName());
    network = new Network(username, textProvider);
    quit = false;
  }

  public void run() {
    while (!quit) {
      execute(parser.parse(console.askForInput(textProvider.askForInput(network.getCurrentClient().getName()))));
    }
  }


  public void execute(Action action) {
    switch (action.getAction()) {
      case QUIT -> quit = true;
      case CLEAR -> clear();
      case HELP -> help();
      case MINER -> addMiner();
      case CLIENT -> clientAction(action);
      case WHITESPACE -> Console.print("");
      case UNKNOWN_COMMAND -> unknownCommand(action);
      case BLOCK_ACTION -> invokeBlockAction(action);
      case TRANSACTION_ACTION -> invokeTransactionAction(action);
      }
    }

  private void clientAction(Action action) {
    switch (action.getSubType()) {
      case CREATE -> createClient(action);
      case SWITCH_CLIENT -> switchClient(action);

      default -> unknownCommand(action);
    }
  }

  private void switchClient(Action action) {
    List<String> args = action.getArgs();
    if(args.size() != 1) {
      unknownCommand(action);
    } else {
      try {
        network.changeClient(args.get(0));
      } catch (Exception e) {
        Console.printLine(textProvider.incorrectArgumentsUsage(List.of("[name]")));
      }
    }
  }

  private void createClient(Action action) {
    List<String> args = action.getArgs();
    if(args.size() != 1) {
      unknownCommand(action);
    } else {
      try {
        network.addClient(args.get(0));
      } catch (KeyAlreadyExistsException e) {
        Console.printLine(textProvider.incorrectArgumentsUsage(List.of("[name]")));
      }
    }
  }

  private void addMiner() {
    network.addMiner();
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

  private void validateTransaction(Action action) {
    try {
      if(action.getArgs().size()!=1) {
        Console.error(textProvider.incorrectArgumentsUsage(List.of("[transaction_index]")));
        return;
      }
      int index = Integer.parseInt(action.getArgs().get(0));
      Transaction transaction = allTransactions.get(index);
      network.validateTransaction(transaction);
    } catch (Exception e) {
      Console.error(textProvider.incorrectArgumentsUsage(List.of("[transaction_index]")));
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
        .from(network.getCurrentClient().getName())
        .to(args.get(0))
        .timestamp(timestamp)
        .type(TransactionType.USER_TRANSACTION)
        .amount(amount)
        .signature(network.getCurrentClient().sign((new Date(123123123L)).toString().getBytes(StandardCharsets.UTF_8)))
        .build();
    allTransactions.add(transaction);
    Console.info(textProvider.invalidTransactionCreationSuccessful() + textProvider.transactionName(transaction));
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
        .from(network.getCurrentClient().getName())
        .to(args.get(0))
        .timestamp(timestamp)
        .type(TransactionType.USER_TRANSACTION)
        .amount(amount)
        .signature(network.getCurrentClient().sign(timestamp.toString().getBytes(StandardCharsets.UTF_8)))
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
    try {
      if(action.getArgs().size()!=1) {
        Console.error(textProvider.incorrectArgumentsUsage(List.of("[transaction_index]")));
        return;
      }

      int index = Integer.parseInt(action.getArgs().get(0));
      Transaction transaction = allTransactions.get(index);
      Block block = Block.builder()
          .transaction(transaction)
          .hash("0000aa9298d75c97e38f11d2f661b307da0cd466fbe02b2dbe9ec91d187e5679")
          .nonce(11515)
          .timeStamp(new Date().getTime())
          .previousHash(network.getCurrentClient()
              .getChain()
              .get(network.getCurrentClient().getChain().size() -1)
              .getPreviousHash())
          .build();

      network.validateBlock(block);
    } catch (Exception e) {
      Console.error(textProvider.incorrectArgumentsUsage(List.of("[transaction_index]")));
    }
  }

  private void createBlock(Action action) {
    try {
      if(action.getArgs().size()!=1) {
        Console.error(textProvider.incorrectArgumentsUsage(List.of("[transaction_index]")));
        return;
      }
      int index = Integer.parseInt(action.getArgs().get(0));
      Transaction transaction = allTransactions.get(index);
      network.mineBlock(transaction);
    } catch (Exception e) {
      Console.error(textProvider.incorrectArgumentsUsage(List.of("[transaction_index]")));
    }
  }


  private void getShortListOfBlocks(Action action) {
    if(!action.getArgs().isEmpty()) {
      try {
       int index = Integer.parseInt(action.getArgs().get(0));
       Block block = network.getCurrentClient().getChain().get(index);
        Console.printLine(index + ". " + textProvider.shortBlockTransactionMined(block));
      } catch (Exception e) {
        Console.error(textProvider.incorrectArgumentsUsage(List.of("[block_index]")));
      }
    } else {
      for(int i=0;i<network.getCurrentClient().getChain().size(); i++) {
        Block block = network.getCurrentClient().getChain().get(i);
        Console.printLine(i + ". " + textProvider.shortBlockTransactionMined(block));
      }
    }
  }

  private void getLongListOfBlocks(Action action) {
    try {
    if(action.getArgs().isEmpty()) {
        for(int i=0;i<network.getCurrentClient().getChain().size(); i++) {
          Block block = network.getCurrentClient().getChain().get(i);
          Console.printJSON(
              List.of("index", "transaction", "mined at", "nonce", "prev hash", "hash"),
              List.of(i, block.getTransaction().toString(), block.getTimeStamp(), block.getNonce(),
                  block.getPreviousHash(), block.getHash()));
        }
      } else {
          int index = Integer.parseInt(action.getArgs().get(0));
          Block block = network.getCurrentClient().getChain().get(index);
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

}
