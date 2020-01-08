package com.github.malyszaryczlowiek.cpcdb.compound;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum TempStability
{
    NS("Not Selected"), RT("RT"), FRIDGE("0 C"), FREEZER("-20 C");

    private String abbreviation;

    TempStability(String abbreviation)
    {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation()
    {
        return this.abbreviation;
    }

    @Override
    public String toString()
    {
        return this.abbreviation;
    }

    public static TempStability stringToEnum(String s)
    {
        switch (s)
        {
            case "Not Selected":
                return NS;
            case "RT":
                return RT;
            case "0 C":
                return FRIDGE;
            case "-20 C":
                return FREEZER;
            default:
                return NS;
        }
    }

    public static List<String> returnValues()
    {
        return  Arrays.stream(new String[]{NS.toString(), RT.toString(), FRIDGE.toString(), FREEZER.toString()})
                .collect(Collectors.toList());
    }
}
