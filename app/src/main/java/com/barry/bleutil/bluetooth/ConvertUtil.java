package com.barry.bleutil.bluetooth;

import java.text.DecimalFormat;

public class ConvertUtil {
    public static byte[] extractBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }

    public static String hexToString(String hex)
    {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        for( int i=0; i<hex.length()-1; i+=2 ){

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            if(!output.equals("00"))
            {
                //convert hex to decimal
                int decimal = Integer.parseInt(output, 16);
                //convert the decimal to character
                sb.append((char)decimal);
                temp.append(decimal);
            }
        }
        return sb.toString();
    }
    public final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String byteTohex(byte[] b) {
        char[] hexChars = new char[b.length * 2];
        for ( int j = 0; j < b.length; j++ ) {
            int v = b[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);

    }
    public static String byteTohex(byte b) {
        char[] hexChars = new char[2];
        for ( int j = 0; j < 1; j++ ) {
            int v = b & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);

    }
    public static byte[] hexStringToByteArray(String s) {
        if(s != null)
        {
            int len = s.length();
            if(len % 2 == 0)
            {
                byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                            + Character.digit(s.charAt(i+1), 16));
                }
                return data;
            }else
            {
                return null;
            }
        }else
        {
            return null;
        }
    }
}
