package com.github.malyszaryczlowiek.cpcdb.alerts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ErrorFlagsManager
{
    private static ConcurrentMap<ErrorFlags, Boolean> mapOfErrors = null;

    public static void initializeErrorFlagsManager() {
        if (mapOfErrors == null) {
            ErrorFlags[] flags = ErrorFlags.values();
            mapOfErrors = new ConcurrentHashMap<>(flags.length);
            for (ErrorFlags flags1: flags) {
                mapOfErrors.put(flags1, false);
            }
        }
    }
    public static Map<ErrorFlags, Boolean> getMapOfErrors() {
        return mapOfErrors;
    }

    public static void setErrorFlagTo(ErrorFlags flag, boolean value) {
        mapOfErrors.replace(flag, value);
    }

    public static boolean getError(ErrorFlags flag) {
        return mapOfErrors.get(flag);
    }
}
