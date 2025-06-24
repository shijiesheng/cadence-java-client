/*
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package com.uber.cadence.internal.external;

import com.uber.cadence.converter.DataConverter;
import com.uber.cadence.entities.WorkflowExecution;
import com.uber.cadence.internal.metrics.MetricsTag;
import com.uber.cadence.serviceclient.IWorkflowServiceV4;
import com.uber.m3.tally.Scope;
import com.uber.m3.util.ImmutableMap;
import java.util.Map;
import java.util.Objects;

public class ManualActivityCompletionClientFactoryImpl
    extends ManualActivityCompletionClientFactory {

  private final IWorkflowServiceV4 service;
  private final DataConverter dataConverter;
  private final String domain;
  private final Scope metricsScope;

  public ManualActivityCompletionClientFactoryImpl(
      IWorkflowServiceV4 service, String domain, DataConverter dataConverter, Scope metricsScope) {
    this.service = Objects.requireNonNull(service);
    this.domain = Objects.requireNonNull(domain);
    this.dataConverter = Objects.requireNonNull(dataConverter);

    Map<String, String> tags =
        new ImmutableMap.Builder<String, String>(1).put(MetricsTag.DOMAIN, domain).build();
    this.metricsScope = metricsScope.tagged(tags);
  }

  public IWorkflowServiceV4 getService() {
    return service;
  }

  public DataConverter getDataConverter() {
    return dataConverter;
  }

  @Override
  public ManualActivityCompletionClient getClient(byte[] taskToken) {
    if (taskToken == null || taskToken.length == 0) {
      throw new IllegalArgumentException("null or empty task token");
    }
    return new ManualActivityCompletionClientImpl(
        service, domain, taskToken, dataConverter, metricsScope);
  }

  @Override
  public ManualActivityCompletionClient getClient(WorkflowExecution execution, String activityId) {
    if (execution == null) {
      throw new IllegalArgumentException("null execution");
    }
    if (activityId == null) {
      throw new IllegalArgumentException("null activityId");
    }
    return new ManualActivityCompletionClientImpl(
        service, domain, execution, activityId, dataConverter, metricsScope);
  }
}
