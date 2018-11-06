package com.sequenceiq.caas;

public class JwtSpecData {
    public static final byte[] HMAC_KEY;

    static {
        int[] keyInts = new int[] {3, 35, 53, 75, 43, 15, 165, 188, 131, 126, 6, 101, 119, 123, 166, 143, 90, 179, 40, 230, 240, 84, 201, 40, 169, 15, 132, 178, 210, 80, 46, 191, 211, 251, 90, 146, 210, 6, 71, 239, 150, 138, 180, 195, 119, 98, 61, 34, 61, 46, 33, 114, 5, 46, 79, 8, 192, 205, 154, 245, 103, 208, 128, 163};
        HMAC_KEY = new byte[keyInts.length];

        for (int i=0; i < keyInts.length; i++) {
            HMAC_KEY[i] = (byte)keyInts[i];
        }
    }
}
