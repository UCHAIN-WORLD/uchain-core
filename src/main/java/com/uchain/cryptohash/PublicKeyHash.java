package com.uchain.cryptohash;

public class PublicKeyHash {

	public static String toAddress(byte[] hash) {
		if (hash.length == 20) {
			return Base58Check.encode(CryptoUtil.binaryData2array(CryptoUtil.fromHexString("0f50")), hash);
		}
		throw new IllegalArgumentException();
	}
	
	public static UInt160 fromAddress(String address) {
		if (address.startsWith("UC") && address.length() == 35) {
			byte[] decode = Base58Check.decode(address);
			 if (decode.length == 22) {
				 byte[] dest = new byte[20];
				 System.arraycopy(decode, 2, dest, 0, 20);
				 return UInt160.fromBytes(dest);
			 }
		}else {
			return null; 
		 }
		return null;
	}

}
