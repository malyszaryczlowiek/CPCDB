package com.github.malyszaryczlowiek.cpcdb.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SystemLoader
{
    public void createSystemDirectory()
    {
        try
        {
            Files.createDirectory(Path.of( System.getProperty("user.home") + System.getProperty("file.separator")
                    + "cpcdb"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
