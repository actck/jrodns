package tech.stacktrace.jrodns;

public class Utils {

    public static byte[] trimByteArray(byte[] old, int length) {
        byte ret[] = new byte[length];
        System.arraycopy(old, 0, ret, 0, length);
        return ret;
    }

}
