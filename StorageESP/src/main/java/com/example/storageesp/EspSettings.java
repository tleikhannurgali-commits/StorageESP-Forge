package com.example.storageesp;

import java.util.Properties;

/** Individually configurable details for the ESP modules. */
public final class EspSettings {
    private EspSettings() {}

    public static boolean playerHealth = true;
    public static boolean playerTracer = false;
    public static boolean playerDistance = true;
    public static boolean storageTracer = false;

    static void load(Properties p) {
        playerHealth = Boolean.parseBoolean(p.getProperty("esp.playerHealth", "true"));
        playerTracer = Boolean.parseBoolean(p.getProperty("esp.playerTracer", "false"));
        playerDistance = Boolean.parseBoolean(p.getProperty("esp.playerDistance", "true"));
        storageTracer = Boolean.parseBoolean(p.getProperty("esp.storageTracer", "false"));
    }

    static void save(Properties p) {
        p.setProperty("esp.playerHealth", String.valueOf(playerHealth));
        p.setProperty("esp.playerTracer", String.valueOf(playerTracer));
        p.setProperty("esp.playerDistance", String.valueOf(playerDistance));
        p.setProperty("esp.storageTracer", String.valueOf(storageTracer));
    }
}
