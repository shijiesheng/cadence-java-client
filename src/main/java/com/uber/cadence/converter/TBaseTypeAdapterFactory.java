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

package com.uber.cadence.converter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.uber.cadence.entities.BaseError;
import com.uber.m3.tally.Scope;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TJSONProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special handling of TBase message serialization and deserialization. This is to support for
 * inline Thrift fields in Java class.
 */
public class TBaseTypeAdapterFactory implements TypeAdapterFactory {

  private static final Logger logger = LoggerFactory.getLogger(TBaseTypeAdapterFactory.class);
  private final Scope metricsScope;

  public TBaseTypeAdapterFactory(Scope metricsScope) {
    this.metricsScope = metricsScope;
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    // this class only serializes 'TBase' and its subtypes
    if (!TBase.class.isAssignableFrom(typeToken.getRawType())) {
      return null;
    }
    TypeAdapter<T> result =
        new TypeAdapter<T>() {
          @Override
          public void write(JsonWriter jsonWriter, T value) throws IOException {
            if (metricsScope != null) {
              metricsScope.counter("tbase_message_write").inc(1);
            }
            try {
              String result =
                  newThriftSerializer().toString((TBase) value, StandardCharsets.UTF_8.name());
              jsonWriter.value(result);
              logger.warn(
                  "TBase message will no longer be support in cadence-java-client V4, payload {}",
                  result);
            } catch (BaseError e) {
              throw new DataConverterException("Failed to serialize TBase", e);
            }
          }

          @Override
          public T read(JsonReader jsonReader) throws IOException {
            if (metricsScope != null) {
              metricsScope.counter("tbase_message_read").inc(1);
            }
            String value = jsonReader.nextString();
            try {
              logger.warn(
                  "TBase message will no longer be support in cadence-java-client V4, payload {}",
                  value);
              @SuppressWarnings("unchecked")
              T instance = (T) typeToken.getRawType().getConstructor().newInstance();
              newThriftDeserializer()
                  .deserialize((TBase) instance, value, StandardCharsets.UTF_8.name());
              return instance;
            } catch (Exception e) {
              throw new DataConverterException("Failed to deserialize TBase", e);
            }
          }
        }.nullSafe();
    return result;
  }

  private static TSerializer newThriftSerializer() {
    return new TSerializer(new TJSONProtocol.Factory());
  }

  private static TDeserializer newThriftDeserializer() {
    return new TDeserializer(new TJSONProtocol.Factory());
  }
}
