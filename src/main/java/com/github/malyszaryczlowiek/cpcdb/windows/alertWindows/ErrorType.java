package com.github.malyszaryczlowiek.cpcdb.windows.alertWindows;

public enum ErrorType
{
    CANNOT_CONNECT_TO_ALL_DB,
    CANNOT_CONNECT_TO_REMOTE_BD_AND_INCORRECT_LOCAL_PASSPHRASE,
    CANNOT_CONNECT_TO_REMOTE_DB,
    INCORRECT_REMOTE_PASSPHRASE_AND_CANNOT_CONNECT_TO_LOCAL_DB,
    INCORRECT_REMOTE_AND_LOCAL_PASSPHRASE,
    INCORRECT_REMOTE_PASSPHRASE,
    UNKNOWN_ERROR_OCCURRED,
    INCORRECT_PORT_NUMBER_FORMAT,
    NOT_FOUND_COMPOUND
}
