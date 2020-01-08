package com.github.malyszaryczlowiek.cpcdb.compound;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Unit
{
    NS("Not Selected"), mg("mg"), g("g"), kg("kg"),
    ml("ml"), l("l");

    private String abbreviation;

    Unit(String abbreviation)
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

    public static Unit stringToEnum(String s)
    {
        switch (s)
        {
            case "mg":
                return mg;
            case "g":
                return g;
            case "kg":
                return kg;
            case "ml":
                return ml;
            case "l":
                return l;
            default:
                return NS;
        }
    }

    public static List<String> returnValues()
    {
        return  Arrays.stream(new String[]{NS.toString(), mg.toString(), g.toString(), kg.toString(),
                ml.toString(), l.toString()}).collect(Collectors.toList());
    }
}
