package com.github.malyszaryczlowiek.cpcdb.tasks;

import javafx.concurrent.Task;

public class SaveCurrentRemoteDbToLocalDb extends Task<String>
{

    public static Task<String> getTask() {
        SaveCurrentRemoteDbToLocalDb task = new SaveCurrentRemoteDbToLocalDb();
        task.setUpListeners();
        return task;
    }

    private SaveCurrentRemoteDbToLocalDb() {}

    private void setUpListeners() {

    }

    @Override
    protected String call() {

        return null;
    }
}
