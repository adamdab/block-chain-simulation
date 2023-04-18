package org.pw.simulation.clients;


import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.crypto.Cipher;
import lombok.Getter;
import org.pw.simulation.entity.Block;
import org.pw.simulation.entity.Transaction;

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

  public Transaction createTransaction(String recipient, int amount) {
    return Transaction.builder()
        .from(name)
        .to(recipient)
        .amount(amount)
        .timestamp(new Date().getTime())
        .build();
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

}
