package com.uchain.util;

import com.uchain.uvm.DataWord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Utils {
    private static final DataWord DIVISOR = DataWord.of(64);

    public static int uint8(InputStream input) throws IOException {
        return input.read();
    }

   public static void writeUInt8(int input, OutputStream out) throws IOException{
        out.write(input & 0xff);
   }

    public static int uint16(InputStream input) throws IOException {
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        return uint16(input,order);
    }

    public static int uint16(InputStream input, ByteOrder order) throws IOException {
        byte[] bin = new byte[2];
        input.read(bin);
        return uint16(bin, order);
    }

    public static int uint16(byte[] input, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.wrap(input).order(order);
        return buffer.getShort() & 0xFFFF;
    }

    public static  void writeUInt16(int input,OutputStream out) throws IOException{
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        writeUInt16(input,out,order);
    }

    public static  void writeUInt16(int input,OutputStream out,ByteOrder order)throws IOException{
        out.write(writeUInt16(input,order));
    }

    public static byte[] writeUInt16(Integer input,ByteOrder order){
        byte[] bin = new byte[2];
        ByteBuffer buffer = ByteBuffer.wrap(bin).order(order);
        buffer.putShort(input.shortValue());
        return bin;
    }



    public static Long uint32(InputStream input)throws IOException{
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        return uint32(input,order);
    }

    public static Long uint32(InputStream input, ByteOrder order) throws IOException{
        byte[] bin = new byte[4];
        input.read(bin);
        return uint32(bin, order);
    }
    public static Long uint32(byte[] input, ByteOrder order){
        ByteBuffer buffer = ByteBuffer.wrap(input).order(order);
        return buffer.getInt() & 0xFFFFFFFFL;
    }

    public static void writeUInt32(Long input,OutputStream out)throws IOException{
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        writeUInt32(input,out,order);
    }

    public static void writeUInt32(Long input,OutputStream out, ByteOrder order)throws IOException{
        out.write(writeUInt32(input, order));
    }

    public static byte[] writeUInt32(Long input, ByteOrder order){
        byte[] bin = new byte[4];
        ByteBuffer buffer = ByteBuffer.wrap(bin).order(order);
        buffer.putInt((int)(input & 0xffffffff));
        return bin;
    }

    public static byte[] writeUInt32(Long input){
        return writeUInt32(input, ByteOrder.LITTLE_ENDIAN);
    }

    public static Long uint64(InputStream input)throws IOException{
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        return uint64(input,order);
    }

    public static Long uint64(InputStream input, ByteOrder order) throws IOException{
        byte[] bin = new byte[8];
        input.read(bin);
        return uint64(bin, order);
    }

    public static Long uint64(byte[] input, ByteOrder order){
        ByteBuffer buffer = ByteBuffer.wrap(input).order(order);
        return buffer.getLong();
    }

    public static void writeUInt64(long input,OutputStream out)throws IOException{
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        writeUInt64(input,out,order);
    }

    public static void writeUInt64(long input,OutputStream out, ByteOrder order)throws IOException{
        out.write(writeUInt64(input, order));
    }

    public static byte[] writeUInt64(long input,ByteOrder order){
        byte[] bin = new byte[8];
        ByteBuffer buffer = ByteBuffer.wrap(bin).order(order);
        buffer.putLong(input);
        return bin;
    }

    public static Long readVarInt(byte[] blob)throws IOException{
        ByteArrayInputStream in = new ByteArrayInputStream(blob);
        return readVarInt(in);
    }

    public static Long readVarInt(InputStream input)throws IOException{
        long value = input.read();

        if(value < 0xfd) {

        }else if(value == 0xfd){
            value = uint16(input);
        }else if(value == 0xfe){
            value = uint32(input);
        }else if(value == 0xff){
            value = uint64(input);
        }

        return value;
    }

    public static byte[] readByteArray(InputStream input) throws IOException {
        byte[] bytes = new byte[readVarInt(input).intValue()];
        Arrays.fill(bytes, (byte)0);
        input.read(bytes, 0, bytes.length);
        return bytes;
//        val bytes = Array.fill(is.readVarInt)(0.toByte)
//        is.read(bytes, 0, bytes.length)
//        bytes
    }

    public static void writeVarint(Integer input,OutputStream out)throws IOException{
        writeVarint(input.longValue(),out);
    }

    public static void writeVarint(Long input,OutputStream out)throws IOException{
        if(input < 0xfdL){
            writeUInt8(input.intValue(),out);
        }else if (input < 65535L) {
            writeUInt8(0xfd,out);
            writeUInt16(input.intValue(), out);
        }
        else if (input < 1048576L) {
            writeUInt8(0xfe,out);
            writeUInt32(input.longValue(), out);
        }
        else {
            writeUInt8(0xff,out);
            writeUInt64(input, out);
        }
    }

    public static byte[] bytes(InputStream input,Long size) throws IOException{
        return bytes(input,size.intValue());
    }

    public static byte[] bytes(InputStream input,int size) throws IOException{
        byte[] blob = new byte[size];
        if(size > 0){
            int count = input.read(blob);
            if(count < size){
                throw new IOException("not enough data to read from");
            }
        }
        return blob;
    }

    public static void writeBytes(byte[] input,OutputStream out)throws IOException{
        out.write(input);
    }

    public static String varString(InputStream input)throws IOException{
        long length = readVarInt(input);
        return new String(bytes(input,length),"UTF-8");
    }

    public static void writeVarstring(String input,OutputStream out)throws IOException{
        writeVarint(input.length(),out);
        writeBytes(input.getBytes("UTF-8"),out);
    }

    public static byte[] hash(InputStream input)throws IOException{
        return bytes(input,32);
    }

    public static byte[] script(InputStream input)throws IOException{
        long length = readVarInt(input);
        return bytes(input,(int)length);
    }

    public static void writeScript(byte[] input,OutputStream out)throws IOException{
        writeVarint(input.length,out);
        writeBytes(input,out);
    }

    public static String align(String s, char fillChar, int targetLen, boolean alignRight) {
        if (targetLen <= s.length()) return s;
        String alignString = repeat("" + fillChar, targetLen - s.length());
        return alignRight ? alignString + s : s + alignString;
    }

    public static String repeat(String s, int n) {
        if (s.length() == 1) {
            byte[] bb = new byte[n];
            Arrays.fill(bb, s.getBytes()[0]);
            return new String(bb);
        } else {
            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < n; i++) ret.append(s);
            return ret.toString();
        }
    }
    public static DataWord allButOne64th(DataWord dw) {
        DataWord divResult = dw.div(DIVISOR);
        return dw.sub(divResult);
    }
}
