package com.uchain.crypto.jce;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

public final class SpongyCastleProvider {

  private static class Holder {
    private static final Provider INSTANCE;
    static{
        Provider p = Security.getProvider("SC");
        
        INSTANCE = (p != null) ? p : new BouncyCastleProvider();
            
        INSTANCE.put("MessageDigest.UCHAIN-KECCAK-256", "com.uchain.crypto.cryptohash.Keccak256");
        INSTANCE.put("MessageDigest.UCHAIN-KECCAK-512", "com.uchain.crypto.cryptohash.Keccak512");
    }
  }

  public static Provider getInstance() {
    return Holder.INSTANCE;
  }
}
