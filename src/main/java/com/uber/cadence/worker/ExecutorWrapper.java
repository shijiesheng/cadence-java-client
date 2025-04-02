/*
 *  Modifications Copyright (c) 2017-2021 Uber Technologies Inc.
 *  Portions of the Software are attributed to Copyright (c) 2020 Temporal Technologies Inc.
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.uber.cadence.worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public interface ExecutorWrapper {
  ExecutorService wrap(ExecutorService delegate);

  ThreadPoolExecutor wrap(ThreadPoolExecutor delegate);

  ScheduledExecutorService wrap(ScheduledExecutorService delegate);

  static ExecutorWrapper newDefaultInstance() {
    return new ExecutorWrapper() {
      @Override
      public ExecutorService wrap(ExecutorService delegate) {
        return delegate;
      }

      @Override
      public ThreadPoolExecutor wrap(ThreadPoolExecutor delegate) {
        return delegate;
      }

      @Override
      public ScheduledExecutorService wrap(ScheduledExecutorService delegate) {
        return delegate;
      }
    };
  }
}
