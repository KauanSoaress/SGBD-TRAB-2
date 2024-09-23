package com.sgbd.models.lockTypes;

public enum LockTypes {
    READ,
    WRITE,
    CERTIFY,
    UPDATE,
    READ_INTENT,
    WRITE_INTENT,
    UPDATE_INTENT,
    CERTIFY_INTENT,
    COMMIT
}
