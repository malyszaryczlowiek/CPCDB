package com.github.malyszaryczlowiek.cpcdb.buffer;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;

import java.util.Map;
import java.util.Set;

public interface BufferState
{
    <T> void addChange(ActionType actionType, Map<Integer, Compound> mapOfCompounds, Field field, T newValue);
    //<T> void addMapOfChanges(ActionType actionType, Map<Integer, Compound> mapOfCompounds, Map<Field, T> mapOfChanges);
    void addSetOfChanges(Set<Change> changeSet);
    int returnCurrentIndex();
    int getBufferSize();
}
