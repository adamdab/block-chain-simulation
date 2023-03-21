package org.pw.simulation.miners;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pw.simulation.entity.Block;

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
      System.out.println("Error in hash calculation: " + e.getMessage());
    }
    StringBuffer buffer = new StringBuffer();
    for (byte b : bytes) {
      buffer.append(String.format("%02x", b));
    }
    return buffer.toString();
  }

  public Block mineBlock(Long timeStamp, String transactions, int prefix) {
    int nonce = 0;
    String hash = calculateHash(timeStamp, nonce, transactions);
    String prefixString = new String(new char[prefix]).replace('\0', '0');
    while (!hash.substring(0, prefix).equals(prefixString)) {
      nonce++;
      hash = calculateHash(timeStamp, nonce, transactions);
    }
    Block block = Block.builder()
        .hash(hash)
        .nonce(nonce)
        .transactions(transactions)
        .timeStamp(timeStamp)
        .previousHash(prevHash)
        .build();
    prevHash = hash;
    return block;
  }
}
