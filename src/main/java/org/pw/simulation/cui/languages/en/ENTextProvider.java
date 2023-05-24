package org.pw.simulation.cui.languages.en;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.pw.simulation.cui.console.ConsoleColors;
import org.pw.simulation.cui.languages.TextProvider;
import org.pw.simulation.network.entity.Block;
import org.pw.simulation.network.entity.Transaction;

public class ENTextProvider implements TextProvider {

  @Override
  public String askForClientsName() {
    return "Client's name (You, single word) : ";
  }

  @Override
  public String incorrectArgumentsUsage(List<String> arguments) {
    return "Incorrect arguments, use : " +
        arguments.stream()
            .map(Objects::toString)
            .collect(Collectors.joining(" or "));
  }

  @Override
  public String validation(boolean isValid) {
    return isValid? " is VALID" : " is INVALID";
  }

  @Override
  public String invalidTransactionCreationSuccessful() {
    return "Invalid transaction created successfully : ";
  }

  @Override
  public String validTransactionCreationSuccessful() {
    return "Valid transaction created successfully : ";
  }

  @Override
  public String shortPaymentTransaction(Transaction transaction) {
    return transaction.getFrom() + " pays "+ transaction.getAmount() + " euro-sponges to "+ transaction.getTo();
  }

  @Override
  public String validating() {
    return "Validating ...";
  }

  @Override
  public String endOfProcess() {
    return "... Done !";
  }

  @Override
  public String shortBlockTransactionMined(Block block) {
    return transactionName(block.getTransaction()) + ", mined at : " + block.getTimeStamp();
  }

  @Override
  public String unknownCommand() {
    return """
        "#---------------------------------#
        "| An unknown command has occurred |"
        "| Please use /h or /help to see   |"
        "| available commands              |"
        "#---------------------------------#
        """;
  }

  @Override
  public String help() {
    return """
          HELP : /help or /h
            DESC : returns list of commands that user can invoke
            
          QUIT : /quit or /q
            DESC : closes the application
            
          CLEAR : /clear or /cls
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
              PARAMETERS : [] or [transaction_index]
              DESC : get short description of transaction that were created
                     if no index is specified it returns list of all transactions
                     
             --list-all or -la
              PARAMETERS : [] or [transaction_index]
              DESC : get long description of transaction that were created
                     if no index is specified it returns list of all transactions
                     
             --validate or -v
              PARAMETERS : [transaction_index]
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
              PARAMETERS : [] or [block_index]
              DESC : get short description of block in blockchain
                     if no index is specified it returns list of all transactions
                     
             --list-all or -la
              PARAMETERS : [] or [block_index]
              DESC : get long description of block in blockchain
                     if no index is specified it returns list of all transactions

          MINER : /miner or /m
            FLAGS :
              --create lub -c
               DESC : creates and adds new miner to the network

          CLIENT : /client or /c
            FLAGS :
              --create or -c
               PARAMETERS : [name]
               DESC : creates new client with specified name and adds it to the network

              --switch or -s
               PARAMETERS : [name]
               DESC : switches to specified client from network
        """;
  }

  @Override
  public String note() {
    return """
        #################################################################################################
        #                               Blockchain simulation project                                   #
        #                              Warsaw University of Technology                                  #
        #                        Faculty of Mathematics and Information Sciences                        #
        #                            Project from the subject cyber-security                            #
        #################################################################################################
        """;
  }

  @Override
  public String bar() {
    return "-------------------------type /q or /quit to exit /h or /help for help---------------------------";
  }

  @Override
  public String transactionName(Transaction transaction) {
    return String.format("Transaction : [%s] %s pays %s %d euro-sponges",
        new Date(transaction.getTimestamp()).toString(), transaction.getFrom(),
        transaction.getTo(), transaction.getAmount());
  }

  @Override
  public String blockName(Block block) {
    return String.format("Block [%s]\n Hash: %s Nonce: %d \n PrevHash: %s\n",
        new Date(block.getTimeStamp()).toString(), block.getHash(),
        block.getNonce(), block.getPreviousHash());
  }

  @Override
  public String askForInput(String username) {
    String GorgeProofing = ConsoleColors.RED_BRIGHT + """
        Add miner to network : /miner --create
        Create transaction : /transaction --create [word] [integer]
        List all transactions : /transaction --list-all
        Add block to blockchain : /block --create [index of transaction]
        List all blocks : /block --list-all
        For more options see help section : /help
        Exit the application : /quit
        """ + ConsoleColors.RESET;
    return GorgeProofing + username+"> ";
  }
}
