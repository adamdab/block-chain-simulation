package org.pw.simulation.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Block {
  private String hash;
  private String previousHash;
  private String transactions;
  private long timeStamp;
  private int nonce;
  @Override
  public String toString() {
    return String.format("Block [%s]\n Hash: %s Nonce: %d \n PrevHash: %s\n",
        new Date(timeStamp).toString(), hash, nonce, previousHash);
  }
}
