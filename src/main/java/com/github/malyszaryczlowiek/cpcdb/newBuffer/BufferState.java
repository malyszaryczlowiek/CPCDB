package com.github.malyszaryczlowiek.cpcdb.newBuffer;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.Field;

import java.util.Map;

public interface BufferState
{
    <T> void addChange(ActionType actionType, Map<Integer, Compound> mapOfCompounds, Field field, T newValue);
    int returnCurrentIndex();
    int getBufferSize();
}
