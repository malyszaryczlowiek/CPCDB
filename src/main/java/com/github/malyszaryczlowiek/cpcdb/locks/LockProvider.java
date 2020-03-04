package com.github.malyszaryczlowiek.cpcdb.locks;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LockProvider
{
    private static Map<LockTypes, Object> mapOfLocks;

    public static LockProvider getLockProvider() { return new LockProvider(); }

    private LockProvider() {
        if (mapOfLocks == null) {
            LockTypes[] lockTypes = LockTypes.values();
            int size = lockTypes.length;
            mapOfLocks = new ConcurrentHashMap<>(size);
            Arrays.stream(lockTypes).forEach( lockType -> {
                final Object object = new Object();
                mapOfLocks.put(lockType, object);
            }
            );
        }
    }

    public Object getLock(LockTypes lockType) throws IllegalArgumentException {
        return mapOfLocks.get(lockType);
    }
}


/*
    public Map<LockTypes, Object> getMapOfLocks() {
        return mapOfLocks;
    }

 */
