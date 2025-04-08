package com.uber.cadence.serviceclient.exceptions;

public class FeatureNotEnabledException extends ServiceClientException {
    private final String featureFlag;

    public FeatureNotEnabledException(String featureFlag) {
        this.featureFlag = featureFlag;
    }

    public String getFeatureFlag() {
        return featureFlag;
    }
}