package com.github.malyszaryczlowiek.cpcdb.alerts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ErrorFlagsManager
{
    private static ConcurrentMap<ErrorFlags, ErrorStatus> mapOfErrors = null;

    public static void initializeErrorFlagsManager() {
        if (mapOfErrors == null) {
            ErrorFlags[] flags = ErrorFlags.values();
            mapOfErrors = new ConcurrentHashMap<>(flags.length);
            for (ErrorFlags flags1: flags) {
                mapOfErrors.put(flags1, new ErrorStatus());
            }
        }
    }
    public static Map<ErrorFlags, ErrorStatus> getMapOfErrors() { return mapOfErrors; }

    public static void setErrorFlagTo(ErrorFlags flag, String errorMessage) {
        mapOfErrors.get(flag).setErrorMessage(errorMessage);
    }

    public static void resetErrorFlag(ErrorFlags flag) { mapOfErrors.get(flag).resetStatus(); }

    public static boolean getError(ErrorFlags flag) { return mapOfErrors.get(flag).getErrorOccurred(); }
}
