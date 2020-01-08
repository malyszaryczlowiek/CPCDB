package com.github.malyszaryczlowiek.cpcdb.compound;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Compound implements Comparable<Compound>
{
    private boolean savedInDatabase = false;
    private boolean isToDelete= false;
    private int id;

    private String smiles;
    private String compoundNumber;
    private float amount;
    private Unit unit;
    private String form;
    private LocalDateTime dateTimeModification;
    private String container;
    private TempStability tempStability;
    private boolean argon;
    private String storagePlace;
    private String additionalInfo;

    // lista fieldsów które będą wymagane do zmiany w bazie danych.
    private ArrayList<Field> listOfFieldsToChange = new ArrayList<>();


    public Compound(String smiles, String compoundNumber, float amount,
                    Unit unit, String form, TempStability tempStability,
                    boolean argon,  String container, String storagePlace, LocalDateTime dateTimeModification,
                    String additionalInfo)
    {
        this.smiles = smiles;
        this.compoundNumber = compoundNumber;
        this.amount = amount;
        this.unit = unit;
        this.form = form;
        this.dateTimeModification = dateTimeModification;
        this.container = container;
        this.tempStability = tempStability;
        this.argon = argon;
        this.storagePlace = storagePlace;
        this.additionalInfo = additionalInfo;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getSmiles()
    {
        return smiles;
    }

    public void setSmiles(String smiles)
    {
        this.smiles = smiles;
    }

    public String getCompoundNumber()
    {
        return compoundNumber;
    }

    public void setCompoundNumber(String compoundNumber)
    {
        this.compoundNumber = compoundNumber;
    }

    public float getAmount()
    {
        return amount;
    }

    public void setAmount(float amount)
    {
        this.amount = amount;
    }

    public Unit getUnit()
    {
        return unit;
    }

    public void setUnit(Unit unit)
    {
        this.unit = unit;
    }

    public String getForm()
    {
        return form;
    }

    public void setForm(String form)
    {
        this.form = form;
    }

    public String getContainer()
    {
        return container;
    }

    public void setContainer(String container)
    {
        this.container = container;
    }

    public TempStability getTempStability()
    {
        return tempStability;
    }

    public boolean isArgon()
    {
        return argon;
    }

    public void setArgon(boolean argon)
    {
        this.argon = argon;
    }

    public void setTempStability(TempStability tempStability)
    {
        this.tempStability = tempStability;
    }

    public String getStoragePlace()
    {
        return storagePlace;
    }

    public void setStoragePlace(String storagePlace)
    {
        this.storagePlace = storagePlace;
    }

    public LocalDateTime getDateTimeModification()
    {
        return dateTimeModification;
    }

    public void setDateTimeModification(LocalDateTime dateTimeModification)
    {
        this.dateTimeModification = dateTimeModification;
    }

    public String getAdditionalInfo()
    {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo)
    {
        this.additionalInfo = additionalInfo;
    }

    public void setFieldToChange(Field field)
    {
        boolean isFieldInserted = listOfFieldsToChange
                .stream()
                .anyMatch(fieldInList -> fieldInList.equals(field));
        if (!isFieldInserted)
            listOfFieldsToChange.add(field);
    }

    public List<Field> getListOfOrderedFieldsToChange()
    {
        return listOfFieldsToChange
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public void setSavedInDatabase(boolean saved)
    {
        savedInDatabase = saved;
    }

    public boolean isSavedInDatabase()
    {
        return savedInDatabase;
    }

    public boolean isToDelete()
    {
        return isToDelete;
    }

    public void setToDelete(boolean toDelete)
    {
        isToDelete = toDelete;
    }





























    @Override
    public String toString()
    {
        return "id: " + id + "; compound number: " + compoundNumber;
    }

    @Override
    public int compareTo(Compound o)
    {
        // todo napisać comparator polegający na porównaniu compound number
        // nr. projektu *1000 nr zw. docelowego *100 i nr. związku w syntezie *1
        return 0;
    }

    /*
        public <T> void addChange(Field field, T newValue) throws IOException
    {
        if (field == Field.SMILES && newValue instanceof String)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            smiles = (String) newValue;
        }
        else if (field == Field.COMPOUNDNUMBER && newValue instanceof String)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            compoundNumber = (String) newValue;
        }
        else if (field == Field.AMOUNT && newValue instanceof Float)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            amount = (Float) newValue;
        }
        else if (field == Field.UNIT && newValue instanceof String)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            unit = Unit.stringToEnum( (String) newValue );
        }
        else if (field == Field.FORM && newValue instanceof String)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            form = (String) newValue;
        }
        else if (field == Field.DATETIMEMODIFICATION && newValue instanceof LocalDateTime)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            dateTimeModification = (LocalDateTime) newValue;
        }
        else if (field == Field.CONTAINER && newValue instanceof String)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            container = (String) newValue;
        }
        else if (field == Field.TEMPSTABILITY && newValue instanceof String)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            tempStability  = TempStability.stringToEnum( (String) newValue );
        }
        else if (field == Field.ARGON && newValue instanceof Boolean)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            argon = (boolean) newValue;
        }
        else if (field == Field.STORAGEPLACE && newValue instanceof String)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            storagePlace  = (String) newValue;
        }
        else if (field == Field.ADDITIONALINFO && newValue instanceof String)
        {
            if (!listOfFieldsToChange.contains(field))
                listOfFieldsToChange.add(field);
            additionalInfo  = (String) newValue;
        }
        else
            throw new IOException("Incorrect 'newValue' type");
    }
     */
}


