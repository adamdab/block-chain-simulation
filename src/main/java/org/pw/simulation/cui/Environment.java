package org.pw.simulation.cui;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
  private boolean quit;


  public Environment() {
    Console.showTitle();
    Console.note();
    Console.beginning();
    miner = new Miner(new ArrayList<>(), "INIT");
    console = new Console();
    parser = new Parser();
    allTransactions = new ArrayList<>();
    String username = console.askForInput("Podaj nazwę klienta (twoją nazwę) : ");
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
      Console.printLine("Niepoprawne argumenty, podaj : [to amount]");
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
    Console.info("Niepoprawna transakcja została stworzona prawidłowo : " + transaction.toString());
  }

  private void validateTransaction(Action action) {
    List<String> args = action.getArgs();
    try {
      int index = Integer.parseInt(args.get(0));
      Transaction transaction = allTransactions.get(index);
      simulateLatency("Broadcasting",5L,120);
      String validation = miner.validate(transaction, client.getPublicKey())? " jest POPRAWNA" : " jest NIEPOPRAWNA";
      Console.info("Transakcja : " + transaction.toString() + validation);
    } catch (Exception e) {
      Console.error("Niepoprawne argumenty, podaj : [index]");
    }
  }

  private void createTransaction(Action action) {
    List<String> args = action.getArgs();
    if(args.size()!=2) {
      Console.error("Niepoprawne argumenty, podaj : [to amount]");
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
    Console.info("Poprawna transakcja została stworzona prawidłowo : " + transaction.toString());
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
        Console.printLine(index+". " + client.getName() + " płaci "+ transaction.getAmount() + " eurogąbki dla "+ transaction.getTo());
      } catch (Exception e) {
        Console.error("Niepoprawne argumenty, podaj : [] lub [index]");
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
        Console.error("Niepoprawne argumenty, podaj [] lub [index]");
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
      Console.info("Sprawdzanie transakcji...");
      simulateLatency("Broadcasting",5L,120);
      boolean isValid = miner.validate(transaction, client.getPublicKey());
      if(!isValid) {
        Console.error("Transakcja jest NIEPOPRAWNA");
        return;
      } else Console.info("Transakcja jest POPRAWNA");
      Block block = miner.mineBlock(new Date().getTime(), transaction, 4);
      if(invalidate) block.invalidateNonce();
      Console.info("Sprawdzanie bloku...");
      if(miner.validateBlock(block)) {
        Console.info("Blok : " + block.toString() + "jest POPRAWNY");
        simulateLatency("Adding block",5L,80);
        client.addBlock(block);
      } else {
        Console.warn("Blok jest NIEPOPRAWNY");
      }

    } catch (Exception e) {
      Console.error("Niepoprawne argumenty, podaj [transaction_index]");
    }
  }


  private void getShortListOfBlocks(Action action) {
    if(!action.getArgs().isEmpty()) {
      try {
       int index = Integer.parseInt(action.getArgs().get(0));
       Block block = client.getChain().get(index);
        Console.printLine(index + ". Transakcja : " + block.getTransaction().toString() + ", wykopana o : " + block.getTimeStamp());
      } catch (Exception e) {
        Console.error("Niepoprawne argumenty, podaj [block_index]");
      }
    } else {
      for(int i=0;i<client.getChain().size(); i++) {
        Block block = client.getChain().get(i);
        Console.printLine(i + ". Transakcja : " + block.getTransaction().toString() + ", wykopana o : " + block.getTimeStamp());
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
        Console.error("Niepoprawne argumenty, podaj [block_index]");
    }
  }


  private void unknownCommand(Action action) {
    Console.printLine("#---------------------------------#");
    Console.printLine("| Wystąpiło nieznane polecenie    |");
    Console.printLine("| Użyj /h lub /help, aby zobaczyć |");
    Console.printLine("| dostępne polecenia              |");
    Console.printLine("#---------------------------------#");
  }

  private void help() {
    Console.printLine("""
          POMOC : /help lub /h
            OPIS : zwraca listę poleceń, które użytkownik może wywołać
            
          ZAKOŃCZ : /quit lub /q
            OPIS : zamyka aplikację
            
          WYCZYŚĆ : /clear lub /cls
            OPIS : czyści konsolę
          
          TRANSAKCJA : /transaction lub /t
            FLAGI :
             --create lub -c
              PARAMETRY : [kwota]
              OPIS : tworzy poprawną transakcję i dodaje ją do listy transakcji
              
             --create-invalid lub -ci
              PARAMETRY : [kwota]
              OPIS: tworzy nieprawidłową transakcję i dodaje ją do listy transakcji
             
             --list lub -ls
              PARAMETRY : [] lub [indeks_transakcji]
              OPIS : pobiera krótki opis utworzonych transakcji
                     jeśli nie podano indeksu, zwraca listę wszystkich transakcji
                     
             --list-all lub -la
              PARAMETRY : [] lub [indeks_transakcji]
              OPIS : pobiera długi opis utworzonych transakcji
                     jeśli nie podano indeksu, zwraca listę wszystkich transakcji
                     
             --validate lub -v
              PARAMETRY : [indeks_transakcji]
              OPIS : sprawdza poprawność podpisu transakcji,
                     tę część wykonuje górnik przed procesem wydobywania
                     
          BLOK : /block lub /b
            FLAGI :
             --create lub -c
              PARAMETRY : [indeks_transakcji]
              OPIS : tworzy poprawny blok i dodaje go do łańcucha bloków
              
             --create-invalid lub -ci
              PARAMETRY : [indeks_transakcji]
              OPIS: tworzy nieprawidłowy blok i próbuje dodać go do łańcucha bloków
             
             --list lub -ls
              PARAMETRY : [] lub [indeks_bloku]
              OPIS : pobiera krótki opis bloku w łańcuchu bloków
                     jeśli nie podano indeksu, zwraca listę wszystkich transakcji
                     
             --list-all lub -la
              PARAMETRY : [] lub [indeks_bloku]
              OPIS : pobiera długi opis bloku w łańcuchu bloków
                     jeśli nie podano indeksu, zwraca listę wszystkich transakcji
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
    Console.print(message + " ... Zakończone !\n");
  }

}
