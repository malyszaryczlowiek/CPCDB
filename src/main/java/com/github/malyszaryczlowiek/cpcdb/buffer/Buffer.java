package com.github.malyszaryczlowiek.cpcdb.buffer;

public interface Buffer
{
    void undo();
    void redo();
}
