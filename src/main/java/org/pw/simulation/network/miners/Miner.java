package org.pw.simulation.network.miners;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import javax.crypto.Cipher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pw.simulation.cui.console.Console;
import org.pw.simulation.network.entity.Block;
import org.pw.simulation.network.entity.Transaction;

@AllArgsConstructor
@Getter
public class Miner {

  private List<Block> chain;
  private String prevHash;
  private String calculateHash(Long timeStamp, int nonce, String transactions) {
    String dataToHash =
        Long.toString(timeStamp)
            + Integer.toString(nonce)
            + transactions;
    MessageDigest digest = null;
    byte[] bytes = null;
    try {
      digest = MessageDigest.getInstance("SHA-256");
      bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      Console.error("Error in hash calculation: " + e.getMessage());
    }
    StringBuffer buffer = new StringBuffer();
    for (byte b : bytes) {
      buffer.append(String.format("%02x", b));
    }
    return buffer.toString();
  }

  public Block mineBlock(Long timeStamp, Transaction transactions, int prefix) {
    int nonce = 0;
    String hash = calculateHash(timeStamp, nonce, transactions.toString());
    String prefixString = new String(new char[prefix]).replace('\0', '0');
    while (!hash.substring(0, prefix).equals(prefixString)) {
      nonce++;
      hash = calculateHash(timeStamp, nonce, transactions.toString());
    }
    Block block = Block.builder()
        .hash(hash)
        .nonce(nonce)
        .transaction(transactions)
        .timeStamp(timeStamp)
        .previousHash(prevHash)
        .build();
    prevHash = hash;
    this.chain.add(block);
    return block;
  }

  public boolean validate(Transaction transaction, PublicKey publicKey) {
    if(transaction.getSignature() == null) return false;
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, publicKey);
      byte[] message = cipher.doFinal(transaction.getSignature());
      return Arrays.equals(message, transaction.getTimestamp().toString().getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      Console.fatalError("Couldn't validate transaction");
      throw new RuntimeException(e);
    }
  }

  public boolean validateBlock(Block block) {
    String hash = block.getHash();
    String tempHash = calculateHash(block.getTimeStamp(), block.getNonce(),
        block.getTransaction().toString());
    if(hash.equals(tempHash)) return true;
    else {
      Console.warn("Hash is not calculated correctly : ");
      Console.printLine("       { nonce : " + block.getNonce() + ",\n"
          + "         hash : " + hash + "}");
      return false;
    }
  }
}
