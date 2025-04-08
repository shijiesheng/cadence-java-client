package com.uber.cadence.serviceclient.exceptions;

public class DomainNotActiveException extends ServiceClientException {
    private final String domain;
    private final String currentCluster;
    private final String activeCluster;

    public DomainNotActiveException(String domain, String currentCluster, String activeCluster) {
        this.domain = domain;
        this.currentCluster = currentCluster;
        this.activeCluster = activeCluster;
    }

    public String getDomain() {
        return domain;
    }

    public String getCurrentCluster() {
        return currentCluster;
    }

    public String getActiveCluster() {
        return activeCluster;
    }
}