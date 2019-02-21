package com.uchain.util;

import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ByteUtil {

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final byte[] ZERO_BYTE_ARRAY = new byte[]{0};
    public static final byte[] ZERO_BYTE_ARRAY_OF_LENGTH20 = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    /**
     * 创建字节的副本并将其追加到b的末尾
     */
    public static byte[] appendByte(byte[] bytes, byte b) {
        byte[] result = Arrays.copyOf(bytes, bytes.length + 1);
        result[result.length - 1] = b;
        return result;
    }

    /**
     * @param b 将其格式化为字节数组的整数
     * @param numBytes 所需字节数组的大小
     * @return numBytes
     */
    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        if (b == null)
            return null;
        byte[] bytes = new byte[numBytes];
        byte[] biBytes = b.toByteArray();
        int start = (biBytes.length == numBytes + 1) ? 1 : 0;
        int length = Math.min(biBytes.length, numBytes);
        System.arraycopy(biBytes, start, bytes, numBytes - length, length);
        return bytes;
    }

    public static byte[] bigIntegerToBytesSigned(BigInteger b, int numBytes) {
        if (b == null)
            return null;
        byte[] bytes = new byte[numBytes];
        Arrays.fill(bytes, b.signum() < 0 ? (byte) 0xFF : 0x00);//填充数组中的每个元素都是(b.signum() < 0 ? (byte) 0xFF : 0x00)
        byte[] biBytes = b.toByteArray();
        int start = (biBytes.length == numBytes + 1) ? 1 : 0;
        int length = Math.min(biBytes.length, numBytes);
        System.arraycopy(biBytes, start, bytes, numBytes - length, length);
        return bytes;
    }

    public static byte[] bigIntegerToBytes(BigInteger value) {
        if (value == null)
            return null;

        byte[] data = value.toByteArray();

        if (data.length != 1 && data[0] == 0) {
            byte[] tmp = new byte[data.length - 1];
            System.arraycopy(data, 1, tmp, 0, tmp.length);
            data = tmp;
        }
        return data;
    }

    public static BigInteger bytesToBigInteger(byte[] bb) {
        return (bb == null || bb.length == 0) ? BigInteger.ZERO : new BigInteger(1, bb);
    }

    public static int matchingNibbleLength(byte[] a, byte[] b) {
        int i = 0;
        int length = a.length < b.length ? a.length : b.length;
        while (i < length) {
            if (a[i] != b[i])
                return i;
            i++;
        }
        return i;
    }

    /**
     * 将long转换为字节数组
     *
     */
    public static byte[] longToBytes(long val) {
        return ByteBuffer.allocate(Long.BYTES).putLong(val).array();
    }

    public static byte[] longToBytesNoLeadZeroes(long val) {
        if (val == 0) return EMPTY_BYTE_ARRAY;
        byte[] data = ByteBuffer.allocate(Long.BYTES).putLong(val).array();
        return stripLeadingZeroes(data);
    }

    /**
     * 将int转换为字节数组
     *
     */
    public static byte[] intToBytes(int val){
        return ByteBuffer.allocate(Integer.BYTES).putInt(val).array();
    }


    public static byte[] intToBytesNoLeadZeroes(int val){
        if (val == 0) return EMPTY_BYTE_ARRAY;
        int lenght = 0;
        int tmpVal = val;
        while (tmpVal != 0){
            tmpVal = tmpVal >>> 8;
            ++lenght;
        }

        byte[] result = new byte[lenght];
        int index = result.length - 1;
        while(val != 0){
            result[index] = (byte)(val & 0xFF);
            val = val >>> 8;
            index -= 1;
        }
        return result;
    }


    /**
     * 将字节数组转换为hex的字符串
     */
    public static String toHexString(byte[] data) {
        return data == null ? "" : Hex.toHexString(data);
    }

    /**
     * 计算包长度
     */
    public static byte[] calcPacketLength(byte[] msg) {
        int msgLen = msg.length;
        return new byte[]{
                (byte) ((msgLen >> 24) & 0xFF),
                (byte) ((msgLen >> 16) & 0xFF),
                (byte) ((msgLen >> 8) & 0xFF),
                (byte) ((msgLen) & 0xFF)};
    }

    public static int byteArrayToInt(byte[] b) {
        if (b == null || b.length == 0)
            return 0;
        return new BigInteger(1, b).intValue();
    }

    public static long byteArrayToLong(byte[] b) {
        if (b == null || b.length == 0)
            return 0;
        return new BigInteger(1, b).longValue();
    }


    public static String nibblesToPrettyString(byte[] nibbles) {
        StringBuilder builder = new StringBuilder();
        for (byte nibble : nibbles) {
            final String nibbleString = oneByteToHexString(nibble);
            builder.append("\\x").append(nibbleString);
        }
        return builder.toString();
    }

    public static String oneByteToHexString(byte value) {
        String retVal = Integer.toString(value & 0xFF, 16);
        if (retVal.length() == 1) retVal = "0" + retVal;
        return retVal;
    }

    public static int numBytes(String val) {

        BigInteger bInt = new BigInteger(val);
        int bytes = 0;

        while (!bInt.equals(BigInteger.ZERO)) {
            bInt = bInt.shiftRight(8);
            ++bytes;
        }
        if (bytes == 0) ++bytes;
        return bytes;
    }

    public static byte[] encodeValFor32Bits(Object arg) {

        byte[] data;

        // check if the string is numeric
        if (arg.toString().trim().matches("-?\\d+(\\.\\d+)?"))
            data = new BigInteger(arg.toString().trim()).toByteArray();
            // check if it's hex number
        else if (arg.toString().trim().matches("0[xX][0-9a-fA-F]+"))
            data = new BigInteger(arg.toString().trim().substring(2), 16).toByteArray();
        else
            data = arg.toString().trim().getBytes();


        if (data.length > 32)
            throw new RuntimeException("values can't be more than 32 byte");

        byte[] val = new byte[32];

        int j = 0;
        for (int i = data.length; i > 0; --i) {
            val[31 - j] = data[i - 1];
            ++j;
        }
        return val;
    }

    public static byte[] encodeDataList(Object... args) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Object arg : args) {
            byte[] val = encodeValFor32Bits(arg);
            try {
                baos.write(val);
            } catch (IOException e) {
                throw new Error("Happen something that should never happen ", e);
            }
        }
        return baos.toByteArray();
    }

    public static int firstNonZeroByte(byte[] data) {
        for (int i = 0; i < data.length; ++i) {
            if (data[i] != 0) {
                return i;
            }
        }
        return -1;
    }

    public static byte[] stripLeadingZeroes(byte[] data) {

        if (data == null)
            return null;

        final int firstNonZero = firstNonZeroByte(data);
        switch (firstNonZero) {
            case -1:
                return ZERO_BYTE_ARRAY;

            case 0:
                return data;

            default:
                byte[] result = new byte[data.length - firstNonZero];
                System.arraycopy(data, firstNonZero, result, 0, data.length - firstNonZero);

                return result;
        }
    }

    public static boolean increment(byte[] bytes) {
        final int startIndex = 0;
        int i;
        for (i = bytes.length - 1; i >= startIndex; i--) {
            bytes[i]++;
            if (bytes[i] != 0)
                break;
        }
        // we return false when all bytes are 0 again
        return (i >= startIndex || bytes[startIndex] != 0);
    }

    public static byte[] copyToArray(BigInteger value) {
        byte[] src = ByteUtil.bigIntegerToBytes(value);
        byte[] dest = ByteBuffer.allocate(32).array();
        System.arraycopy(src, 0, dest, dest.length - src.length, src.length);
        return dest;
    }


    public static ByteArrayWrapper wrap(byte[] data) {
        return new ByteArrayWrapper(data);
    }

    public static byte[] setBit(byte[] data, int pos, int val) {

        if ((data.length * 8) - 1 < pos)
            throw new Error("outside byte array limit, pos: " + pos);

        int posByte = data.length - 1 - (pos) / 8;
        int posBit = (pos) % 8;
        byte setter = (byte) (1 << (posBit));
        byte toBeSet = data[posByte];
        byte result;
        if (val == 1)
            result = (byte) (toBeSet | setter);
        else
            result = (byte) (toBeSet & ~setter);

        data[posByte] = result;
        return data;
    }

    public static int getBit(byte[] data, int pos) {

        if ((data.length * 8) - 1 < pos)
            throw new Error("outside byte array limit, pos: " + pos);

        int posByte = data.length - 1 - pos / 8;
        int posBit = pos % 8;
        byte dataByte = data[posByte];
        return Math.min(1, (dataByte & (1 << (posBit))));
    }

    public static byte[] and(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) throw new RuntimeException("Array sizes differ");
        byte[] ret = new byte[b1.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) (b1[i] & b2[i]);
        }
        return ret;
    }

    public static byte[] or(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) throw new RuntimeException("Array sizes differ");
        byte[] ret = new byte[b1.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) (b1[i] | b2[i]);
        }
        return ret;
    }

    public static byte[] xor(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) throw new RuntimeException("Array sizes differ");
        byte[] ret = new byte[b1.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) (b1[i] ^ b2[i]);
        }
        return ret;
    }

    public static byte[] xorAlignRight(byte[] b1, byte[] b2) {
        if (b1.length > b2.length) {
            byte[] b2_ = new byte[b1.length];
            System.arraycopy(b2, 0, b2_, b1.length - b2.length, b2.length);
            b2 = b2_;
        } else if (b2.length > b1.length) {
            byte[] b1_ = new byte[b2.length];
            System.arraycopy(b1, 0, b1_, b2.length - b1.length, b1.length);
            b1 = b1_;
        }

        return xor(b1, b2);
    }

    public static byte[] merge(byte[]... arrays)
    {
        int count = 0;
        for (byte[] array: arrays)
        {
            count += array.length;
        }

        // Create new array and copy all array contents
        byte[] mergedArray = new byte[count];
        int start = 0;
        for (byte[] array: arrays) {
            System.arraycopy(array, 0, mergedArray, start, array.length);
            start += array.length;
        }
        return mergedArray;
    }

    public static boolean isNullOrZeroArray(byte[] array){
        return (array == null) || (array.length == 0);
    }

    public static boolean isSingleZero(byte[] array){
        return (array.length == 1 && array[0] == 0);
    }


    public static Set<byte[]> difference(Set<byte[]> setA, Set<byte[]> setB){

        Set<byte[]> result = new HashSet<>();

        for (byte[] elementA : setA){
            boolean found = false;
            for (byte[] elementB : setB){

                if (Arrays.equals(elementA, elementB)){
                    found = true;
                    break;
                }
            }
            if (!found) result.add(elementA);
        }

        return result;
    }

    public static int length(byte[]... bytes) {
        int result = 0;
        for (byte[] array : bytes) {
            result += (array == null) ? 0 : array.length;
        }
        return result;
    }

    public static byte[] intsToBytes(int[] arr, boolean bigEndian) {
        byte[] ret = new byte[arr.length * 4];
        intsToBytes(arr, ret, bigEndian);
        return ret;
    }

    public static int[] bytesToInts(byte[] arr, boolean bigEndian) {
        int[] ret = new int[arr.length / 4];
        bytesToInts(arr, ret, bigEndian);
        return ret;
    }

    public static void bytesToInts(byte[] b, int[] arr, boolean bigEndian) {
        if (!bigEndian) {
            int off = 0;
            for (int i = 0; i < arr.length; i++) {
                int ii = b[off++] & 0x000000FF;
                ii |= (b[off++] << 8) & 0x0000FF00;
                ii |= (b[off++] << 16) & 0x00FF0000;
                ii |= (b[off++] << 24);
                arr[i] = ii;
            }
        } else {
            int off = 0;
            for (int i = 0; i < arr.length; i++) {
                int ii = b[off++] << 24;
                ii |= (b[off++] << 16) & 0x00FF0000;
                ii |= (b[off++] << 8) & 0x0000FF00;
                ii |= b[off++] & 0x000000FF;
                arr[i] = ii;
            }
        }
    }

    public static void intsToBytes(int[] arr, byte[] b, boolean bigEndian) {
        if (!bigEndian) {
            int off = 0;
            for (int i = 0; i < arr.length; i++) {
                int ii = arr[i];
                b[off++] = (byte) (ii & 0xFF);
                b[off++] = (byte) ((ii >> 8) & 0xFF);
                b[off++] = (byte) ((ii >> 16) & 0xFF);
                b[off++] = (byte) ((ii >> 24) & 0xFF);
            }
        } else {
            int off = 0;
            for (int i = 0; i < arr.length; i++) {
                int ii = arr[i];
                b[off++] = (byte) ((ii >> 24) & 0xFF);
                b[off++] = (byte) ((ii >> 16) & 0xFF);
                b[off++] = (byte) ((ii >> 8) & 0xFF);
                b[off++] = (byte) (ii & 0xFF);
            }
        }
    }

    public static short bigEndianToShort(byte[] bs) {
        return bigEndianToShort(bs, 0);
    }

    public static short bigEndianToShort(byte[] bs, int off) {
        int n = bs[off] << 8;
        ++off;
        n |= bs[off] & 0xFF;
        return (short) n;
    }

    public static byte[] shortToBytes(short n) {
        return ByteBuffer.allocate(2).putShort(n).array();
    }

    public static byte[] hexStringToBytes(String data) {
        if (data == null) return EMPTY_BYTE_ARRAY;
        if (data.startsWith("0x")) data = data.substring(2);
        if (data.length() % 2 == 1) data = "0" + data;
        return Hex.decode(data);
    }

    public static byte[] hostToBytes(String ip) {
        byte[] bytesIp;
        try {
            bytesIp = InetAddress.getByName(ip).getAddress();
        } catch (UnknownHostException e) {
            bytesIp = new byte[4];  // fall back to invalid 0.0.0.0 address
        }

        return bytesIp;
    }

    public static String bytesToIp(byte[] bytesIp) {

        StringBuilder sb = new StringBuilder();
        sb.append(bytesIp[0] & 0xFF);
        sb.append(".");
        sb.append(bytesIp[1] & 0xFF);
        sb.append(".");
        sb.append(bytesIp[2] & 0xFF);
        sb.append(".");
        sb.append(bytesIp[3] & 0xFF);

        String ip = sb.toString();
        return ip;
    }

    /**
     * 返回最高阶（“最左边”）一位之前的零位
     */
    public static int numberOfLeadingZeros(byte[] bytes) {

        int i = firstNonZeroByte(bytes);

        if (i == -1) {
            return bytes.length * 8;
        } else {
            int byteLeadingZeros = Integer.numberOfLeadingZeros((int)bytes[i] & 0xff) - 24;
            return i * 8 + byteLeadingZeros;
        }
    }

    public static byte[] parseBytes(byte[] input, int offset, int len) {

        if (offset >= input.length || len == 0)
            return EMPTY_BYTE_ARRAY;

        byte[] bytes = new byte[len];
        System.arraycopy(input, offset, bytes, 0, Math.min(input.length - offset, len));
        return bytes;
    }

    public static byte[] parseWord(byte[] input, int idx) {
        return parseBytes(input, 32 * idx, 32);
    }

    public static byte[] parseWord(byte[] input, int offset, int idx) {
        return parseBytes(input, offset + 32 * idx, 32);
    }
}