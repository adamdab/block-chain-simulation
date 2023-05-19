package org.pw.simulation.cui.languages;

import java.util.List;
import org.pw.simulation.network.entity.Block;
import org.pw.simulation.network.entity.Transaction;

public interface TextProvider {
  public String askForClientsName();
  public String incorrectArgumentsUsage(List<String> arguments);
  public String validation(boolean isValid);
  public String invalidTransactionCreationSuccessful();
  public String validTransactionCreationSuccessful();
  public String shortPaymentTransaction(Transaction transaction);
  public String validating();
  public String endOfProcess();
  public String shortBlockTransactionMined(Block block);
  public String unknownCommand();
  public String help();
  public String note();
  public String bar();
  public String transactionName(Transaction transaction);
  public String blockName(Block block);
  public String askForInput(String username);
}
