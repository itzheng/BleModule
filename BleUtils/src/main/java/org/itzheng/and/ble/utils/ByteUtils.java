package org.itzheng.and.ble.utils;

import java.util.ArrayList;

/**
 * Title:<br>
 * Description: <br>
 *
 * @email ItZheng@ZoHo.com
 * Created by itzheng on 2018-1-22.
 */
public class ByteUtils {

    /**
     * @param data
     * @return
     */
    public static byte[] getBytesByString(String data) {
        byte[] bytes = null;
        if (data != null) {
            data = data.toUpperCase();
            int length = data.length() / 2;
            char[] dataChars = data.toCharArray();
            bytes = new byte[length];
            for (int i = 0; i < length; ++i) {
                int pos = i * 2;
                bytes[i] = (byte) (charToByte(dataChars[pos]) << 4 | charToByte(dataChars[pos + 1]));
            }
        }

        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            String hexString = Integer.toHexString(b & 0xff);
            if (hexString.length() < 2) {
                sb.append("0");
            }
            sb.append(hexString);
        }
        return sb.toString();
    }

    public static String toHexString(int bytes) {
        return Integer.toHexString(bytes & 0XFF);
    }

    public static String toHexString(ArrayList<Byte> bytes) {
        if (bytes == null) {
            return "";
        }
        //Byte和byte位数不一样
        byte[] data = listToBytes(bytes);
        return toHexString(data);
    }

    public static byte[] listToBytes(ArrayList<Byte> bytes) {
        if (bytes == null) {
            return null;
        }
//        ArrayList<Byte> newBytes=bytes.
        int length = bytes.size();
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            if (data.length <= i || bytes.size() <= i) {
                break;
            }
            if (bytes == null || bytes.get(i) == null) {
                break;
            }
            data[i] = bytes.get(i);
        }
        return data;
    }

    /**
     * 截取byte数组
     *
     * @param bytes
     * @param fromIndex 从哪个位置截取
     * @param length    截取的长度
     * @return
     */
    public static byte[] subBytes(byte[] bytes, int fromIndex, int length) {
        if (bytes == null) {
            return null;
        }
        byte[] newByte = new byte[length];
        for (int i = 0; i < newByte.length; i++) {
            if (bytes.length < i + fromIndex) {
                break;
            }
            newByte[i] = bytes[i + fromIndex];
        }
        return newByte;
    }
}
