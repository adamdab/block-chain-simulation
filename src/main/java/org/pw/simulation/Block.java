package org.pw.simulation;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Block {
  private String hash;
  private String previousHash;
  private String data;
  private long timeStamp;
  private int nonce;
}
