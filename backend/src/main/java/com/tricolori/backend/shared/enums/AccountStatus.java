package com.tricolori.backend.shared.enums;

import lombok.Getter;

@Getter
public enum AccountStatus {
    ACTIVE("Active"),
    WAITING_FOR_ACTIVATION("Waiting for Activation"),
    SUSPENDED("Suspended");

    private final String displayName;

    AccountStatus(String displayName) {
        this.displayName = displayName;
    }
}