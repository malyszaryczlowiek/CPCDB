package com.github.malyszaryczlowiek.cpcdb.helperClasses;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;

import java.util.Random;

public class DBGenerator
{
    Compound[] compounds = new Compound[100000];

    String[] colors;


    final String[] capacity = new String[] {"5", "10", "25", "50", "100", "250", "500", "1000"};

    final String[] form = new String[] {"bezpostaciowy osad", "kryształy", "olej" };
    final String[] container = new String[] {"fiolka", "kolbka", "kolba", "słoik", "wiaderko", "butelka"};
    final String[] storagePlace = new String[] {"lodówka", "półka", "szuflada", "zamrażarka", "magazyn"};
    final String[] additionalInfo = new String[] {"na stanie", "dorabiany", };

    void generateDB()
    {
        colors = kolory.split("\n");

        Random random = new Random();
        int[] lista = random.ints(100000).toArray();

    }





































    String kolory = "biały\n" +
            "            alabastrowy\n" +
            "    kość słoniowa\n" +
            "    mleczny\n" +
            "            chamois\n" +
            "    kremowy\n" +
            "            perłowy\n" +
            "    porcelanowy\n" +
            "            amarantowy\n" +
            "    arbuzowy\n" +
            "            biskupi\n" +
            "    cyklamen\n" +
            "            eozyna\t\n" +
            "łososiowy\n" +
            "        magenta, mażenta (HTML)\n" +
            "        magenta, mażenta (druk)\n" +
            "        majtkowy\n" +
            "        malinowy\n" +
            "        pąsowy\n" +
            "        róż indyjski\n" +
            "        róż pompejański\n" +
            "        róż wenecki\n" +
            "        różowy\n" +
            "        rubinowy\n" +
            "        bordowy, bordo\n" +
            "        buraczkowy\n" +
            "        burgund\n" +
            "        ceglasty\n" +
            "        cynobrowy, cynober\n" +
            "        czerwień alizarynowa\n" +
            "        czerwień wzrokowa\n" +
            "        czerwień żelazowa\n" +
            "        czerwony\n" +
            "        fuksja\n" +
            "        kardynalski\n" +
            "        karmazynowy\n" +
            "        karminowy\n" +
            "        magenta, mażenta (HTML)\n" +
            "        makowy\n" +
            "        poziomkowy\n" +
            "        rdzawy\n" +
            "        rudy\n" +
            "        szkarłatny\n" +
            "        tango\n" +
            "        truskawkowy\n" +
            "        wiśniowy\n" +
            "        brzoskwiniowy\n" +
            "        bursztynowy\n" +
            "        brązowy\n" +
            "        cynamonowy\n" +
            "        herbaciany\n" +
            "        koralowy\n" +
            "        marchewkowy\n" +
            "        miedziany\n" +
            "        miodowy\n" +
            "        morelowy\n" +
            "        ochra\n" +
            "        oranż\n" +
            "        pomarańczowy\n" +
            "        siena palona\n" +
            "        tycjan\n" +
            "        ugier\n" +
            "        złocisty\n" +
            "        beżowy\n" +
            "        brunatny\n" +
            "        czekoladowy\n" +
            "        heban, hebanowy\n" +
            "        kakaowy\n" +
            "        kasztanowy\n" +
            "        khaki\n" +
            "        mahoń, mahoniowy\n" +
            "        palisander\n" +
            "        orzechowy\n" +
            "        sepia\n" +
            "        spiżowy\n" +
            "        tabaczkowy\n" +
            "        umbra\n" +
            "        bahama yellow\n" +
            "        bananowy\n" +
            "        cytrynowy\n" +
            "        kanarkowy\n" +
            "        piwny\n" +
            "        siarkowy\n" +
            "        słomkowy\n" +
            "        stare złoto\n" +
            "        szafranowy\n" +
            "        zieleń wiosenna\n" +
            "        złoty\n" +
            "        żółty\n" +
            "        żółty (druk)\n" +
            "        malachitowy\n" +
            "        marengo\n" +
            "        miętowy\n" +
            "        morski\n" +
            "        oliwkowy\n" +
            "        patynowy\n" +
            "        pistacjowy\n" +
            "        seledynowy\n" +
            "        szmaragdowy\n" +
            "        trawiasty\n" +
            "        zieleń butelkowa\n" +
            "        zieleń jaskrawa\n" +
            "        zieleń Veronese'a\t\n" +
            "        zieleń zgniła\n" +
            "        zielony\n" +
            "        atramentowy\n" +
            "        błękit królewski\n" +
            "        błękit paryski\n" +
            "        błękit pruski\n" +
            "        błękit Thénarda\n" +
            "        błękit Turnbulla\n" +
            "        chabrowy\n" +
            "        kobaltowy\n" +
            "        lapis-lazuli\n" +
            "        lazurowy\n" +
            "        modry\n" +
            "        niebieski\n" +
            "        siny\n" +
            "        szafirowy\n" +
            "        ultramaryna\n" +
            "        turkusowy\n" +
            "        lawendowy\n" +
            "        ametystowy\n" +
            "        fioletowy\n" +
            "        fiołkowy\n" +
            "        jagodowy\n" +
            "        purpurowy";
}


