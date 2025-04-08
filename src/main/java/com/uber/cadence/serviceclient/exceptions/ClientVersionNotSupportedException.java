package com.uber.cadence.serviceclient.exceptions;

public class ClientVersionNotSupportedException extends ServiceClientException {
    private final String featureVersion;
    private final String clientImpl;
    private final String supportedVersions;

    public ClientVersionNotSupportedException(String featureVersion, String clientImpl, String supportedVersions) {
        this.featureVersion = featureVersion;
        this.clientImpl = clientImpl;
        this.supportedVersions = supportedVersions;
    }

    public String getFeatureVersion() {
        return featureVersion;
    }

    public String getClientImpl() {
        return clientImpl;
    }

    public String getSupportedVersions() {
        return supportedVersions;
    }
}