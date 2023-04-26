package org.pw.simulation.network.clients;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.List;
import java.util.Optional;
import javax.crypto.Cipher;
import lombok.Getter;
import org.pw.simulation.cui.console.Console;
import org.pw.simulation.entity.Block;

public class Client {

  private final Optional<KeyPair> keyPair;
  @Getter
  private final List<Block> chain;
  @Getter
  private final String name;

  public Client(String name, List<Block> chain) {
    this.chain = chain;
    this.name = name;
    KeyPair pair = null;
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      pair = generator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      System.out.println("[FATAL ERROR] Can not create keys for this user");
    }
    keyPair = Optional.ofNullable(pair);
  }

  public PublicKey getPublicKey() {
    return this.keyPair.get().getPublic();
  }

  public void addBlock(Block block) {
    chain.add(block);
  }

  public byte[] sign(byte[] message) {
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, this.keyPair.get().getPrivate());
      return cipher.doFinal(message);
    } catch (Exception e) {
      if( e instanceof NoSuchAlgorithmException) {
        System.out.println("[FATAL ERROR] Can not create RSA cipher");
        throw new RuntimeException("RSA algorithm is missing");
      } else throw new RuntimeException(e);
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


}
