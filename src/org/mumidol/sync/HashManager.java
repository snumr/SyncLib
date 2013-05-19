/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Date: 11.05.13
 * Time: 19:41
 */
public class HashManager {
    private static HashManager manager;
    private Map<String, HashCalculator> calculators = new HashMap<>();

    private HashManager() {
        calculators.put("CRC32", new CRC32HashCalculator());
    }

    public static HashManager getHashManager() {
        if (manager == null) {
            manager = new HashManager();
        }
        return manager;
    }

    public HashCalculator getCalculator(String hashFunc) {
        return calculators.get(getCanonicalName(hashFunc));
    }

    public String getCanonicalName(String hashFunc) {
        String name = hashFunc.toUpperCase();
        switch (name) {
            case "CRC32":
            case "CRC-32":
                return "CRC-32";
            default:
                return name;
        }
    }

    private static class CRC32HashCalculator implements HashCalculator {
        @Override
        public byte[] calculate(InputStream is) throws IOException {
            CRC32 crc = new CRC32();
            byte[] buf = new byte[4096];
            int i = is.read(buf);
            while (i != -1) {
                crc.update(buf, 0, i);
                i = is.read(buf);
            }
            byte[] hash = new byte[4];
            long t = crc.getValue();
            for (int j = 3; j >= 0; j--) {
                hash[j] = (byte) t;
                t >>= 8;
            }
            return hash;
        }
    }
}
