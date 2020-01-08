package com.github.malyszaryczlowiek.cpcdb.util;

import com.github.malyszaryczlowiek.cpcdb.compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.compound.TempStability;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchEngine
{
    private List<Compound> listOfMatchingCompounds;

    public SearchEngine(List<Compound> fullListOfCompounds,String smiles, String smilesAccuracy,
                        String compoundNumber, String compoundNumberAccuracy,
                        String form, String container, String storagePlace,
                        String beforeAfter, LocalDate selectedLocalDate,
                        String argon, String temperature, String additionalInfo)
    {
        listOfMatchingCompounds = fullListOfCompounds
                .parallelStream()
                .filter(compound ->  // filtering via smiles
                {
                    String smilesSearchCriteria = smiles.replaceAll("[ ]+", "");
                    if (smilesSearchCriteria.equals(""))
                        return true;

                    String smilesOfCompound = compound.getSmiles();
                    if (smilesAccuracy.equals("Is Containing"))
                        return smilesOfCompound.contains(smilesSearchCriteria);
                    else // if must match exactly
                        return smilesOfCompound.equals(smilesSearchCriteria);
                })
                .filter(compound -> // filtering via compoundNumber
                {
                    String compoundNumberWithoutSpaces =
                            compoundNumber.trim().replaceAll("[ ]+", "");

                    if (compoundNumberWithoutSpaces.equals(""))
                        return true;

                    switch (compoundNumberAccuracy)
                    {
                        case "Is Containing":
                            return compound.getCompoundNumber()
                                    .toLowerCase()
                                    .contains( compoundNumberWithoutSpaces );
                        case "Is Exactly":
                            return compound.getCompoundNumber()
                                    .toLowerCase()
                                    .equalsIgnoreCase( compoundNumberWithoutSpaces );
                        default:
                            return true;
                    }

                    /*
                    if ( compoundNumberWithoutSpaces.matches("[0-9]{3}[-]?") ||
                            compoundNumberWithoutSpaces.matches("[0-9]{3}[-][0-9]{3}[-]?") ||
                            compoundNumberWithoutSpaces.matches("[0-9]{3}[-][0-9]{3}[-][0-9]{2}[-]?")
                    )
                        return compound.getCompoundNumber()
                                .toLowerCase()
                                .equalsIgnoreCase( compoundNumberWithoutSpaces );
                    else
                        return compound.getCompoundNumber()
                                .toLowerCase()
                                .contains( compoundNumberWithoutSpaces );
                     */
                })
                .filter(compound -> // filtering via form
                {
                    /*
                    remove any ,:;. and additional spaces from form searching keywords
                     */
                    String formWithoutSpaces = form
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .trim()
                            .toLowerCase();


                    if (formWithoutSpaces.equals(""))
                        return true;

                    String formFromCompoundLowercase = compound.getForm()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .trim()
                            .toLowerCase();

                    if ( !formWithoutSpaces.isBlank() && formFromCompoundLowercase.equals(""))
                        return false;

                    return  Arrays.stream(formFromCompoundLowercase.split(" "))
                            .anyMatch( formWithoutSpaces::contains );
                    // insted of lambda:
                    // wordFromCompoundForm ->  formWithoutSpaces.contains(wordFromCompoundForm)
                }) // searching in form
                .filter(compound -> // filtering via temperature stability
                {
                    switch (temperature)
                    { // "Any Temperature", "RT", "Fridge", "Freezer"
                        case "Any Temperature":
                            return true;
                        case "RT":
                            return compound.getTempStability().equals(TempStability.RT);
                        case "Fridge":
                            return compound.getTempStability().equals(TempStability.FRIDGE);
                        case "Freezer":
                            return compound.getTempStability().equals(TempStability.FREEZER);
                        default:
                            return true;
                    }
                })
                .filter(compound -> // filtering via argon stability
                {
                    switch (argon)
                    {
                        //case "Any Atmosphere":
                        //   return true;
                        case "Without Argon":
                            return !compound.isArgon();
                        case "Under Argon":
                            return compound.isArgon();
                        default:
                            return true;
                    }
                })
                .filter(compound ->  // container filter
                {
                    String containerWithoutSpaces = container.trim()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .trim()
                            .toLowerCase();

                    if (containerWithoutSpaces.equals(""))
                        return true;

                    String containerFromCompoundLowercase = compound.getContainer()
                            .trim()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .toLowerCase();

                    if ( !containerWithoutSpaces.isBlank() && containerFromCompoundLowercase.equals(""))
                        return false;

                    return  Arrays.stream(containerFromCompoundLowercase.split(" "))
                            .anyMatch( containerWithoutSpaces::contains );
                    // instead of lambda expression
                    // .anyMatch(wordFromCompoundContainer ->  containerWithoutSpaces.contains(wordFromCompoundContainer)

                }) // container
                .filter(compound -> // filtering via storage place
                {
                    String storagePlaceWithoutSpaces = storagePlace.trim()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .trim()
                            .toLowerCase();

                    if (storagePlaceWithoutSpaces.equals(""))
                        return true;

                    String storagePlaceFromCompoundLowercase = compound.getStoragePlace()
                            .trim()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .toLowerCase();

                    if ( !storagePlaceWithoutSpaces.isBlank() && storagePlaceFromCompoundLowercase.equals(""))
                        return false;

                    return  Arrays.stream(storagePlaceFromCompoundLowercase.split(" "))
                            .anyMatch( storagePlaceWithoutSpaces::contains );
                    // instead of lambda expression:
                    // .anyMatch(wordFromCompoundStoragePlace ->  storagePlaceWithoutSpaces.contains(wordFromCompoundStoragePlace)
                })
                .filter(compound -> // filtering via last modification date
                { // "Before", "After"
                    if (beforeAfter.equals("Before"))
                    {
                        return compound.getDateTimeModification().toLocalDate().isBefore(selectedLocalDate)
                                || compound.getDateTimeModification().toLocalDate().isEqual(selectedLocalDate);
                    }
                    else
                    {
                        return compound.getDateTimeModification().toLocalDate().isAfter(selectedLocalDate);
                        //|| compound.getDateTimeModification().toLocalDate().isEqual(selectedLocalDate);
                    }
                })
                .filter( compound -> // filtering via additional info
                {

                    String additionalInfoWithoutSpaces = additionalInfo.trim()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .trim()
                            .toLowerCase();

                    if (additionalInfoWithoutSpaces.equals(""))
                        return true;

                    String additionalInfoFromCompoundLowercase = compound.getAdditionalInfo()
                            .trim()
                            .replaceAll("[-,;:.}{!@#$%^&*()_|\"\'?<>=+]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .toLowerCase();

                    if ( !additionalInfoWithoutSpaces.isBlank() && additionalInfoFromCompoundLowercase.equals(""))
                        return false;

                    return  Arrays.stream(additionalInfoFromCompoundLowercase.split(" "))
                            .anyMatch( additionalInfoWithoutSpaces::contains );
                })
                .collect(Collectors.toList());
    }

    public List<Compound> returnListOfFoundCompounds()
    {
        return listOfMatchingCompounds;
    }
}
