package com.uchain.networkmanager.message;

public class Message {

    public static byte MessageCode;

    public static byte[] MAGIC = new byte[]{0x12, 0x34, 0x56, 0x78};

    public static int MagicLength = MAGIC.length;

    public static int ChecksumLength = 4;

    public static int HeaderLength = MagicLength + 5;


}