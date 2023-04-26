package org.pw.simulation.cui.languages.pl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.pw.simulation.cui.languages.TextProvider;
import org.pw.simulation.entity.Block;
import org.pw.simulation.entity.Transaction;

public class PolishTextProvider implements TextProvider {

  @Override
  public String askForClientsName() {
    return "Podaj nazwę klienta (ty) : ";
  }

  @Override
  public String incorrectArgumentsUsage(List<String> arguments) {
    return "Niepoprawne argumenty, podaj : " +
        arguments.stream()
            .map(Objects::toString)
            .collect(Collectors.joining(" lub "));
  }

  @Override
  public String validation(boolean isValid) {
    return isValid ? " jest POPRAWNA" : " jest NIEPOPRAWNA";
  }

  @Override
  public String invalidTransactionCreationSuccessful() {
    return "Niepoprawna transakcja została stworzona prawidłowo : ";
  }

  @Override
  public String validTransactionCreationSuccessful() {
    return "Poprawna transakcja została stworzona prawidłowo : ";
  }

  @Override
  public String shortPaymentTransaction(Transaction transaction) {
    return transaction.getFrom() + " płaci "+ transaction.getAmount() + " eurogąbki dla "+ transaction.getTo();
  }

  @Override
  public String validating() {
    return "Sprawdzanie ...";
  }

  @Override
  public String endOfProcess() {
    return " ... Koniec procesu !";
  }

  @Override
  public String shortBlockTransactionMined(Block block) {
    return ". Transakcja : " + block.getTransaction().toString() + ", wykopana o : " + block.getTimeStamp();
  }

  @Override
  public String unknownCommand() {
    return """
        #---------------------------------#
        | Wystąpiło nieznane polecenie    |
        | Użyj /h lub /help, aby zobaczyć |
        | dostępne polecenia              |
        #---------------------------------#
        """;
  }

  @Override
  public String help() {
    return """
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
        """;
  }

  @Override
  public String note() {
    return """
        #################################################################################################
        #                               Projekt symulacji blockchain                                    #
        #                                  Politechnika Warszawska                                      #
        #                       Wydział Matematyki i Nauk Informacyjnych                                #
        #                 Projekt z przedmiotu Inżynieria Cyberbezpieczeństwa                           #
        #################################################################################################
        """;
  }

  @Override
  public String bar() {
    return "---------Należy wpisać /q lub /quit aby wyjść z aplikacji, /h lub /help aby uzyskać pomoc--------";
  }

  @Override
  public String transactionName(Transaction transaction) {
    return String.format("Transakcja : [%s] %s płaci %s %d euro-gąbek",
        new Date(transaction.getTimestamp()).toString(), transaction.getFrom(),
        transaction.getTo(), transaction.getAmount());
  }

  @Override
  public String blockName(Block block) {
    return String.format("Blok [%s]\n Hash: %s Nonce: %d \n PrevHash: %s\n",
        new Date(block.getTimeStamp()).toString(), block.getHash(),
        block.getNonce(), block.getPreviousHash());
  }
}
