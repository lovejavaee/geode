/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.protocol.protobuf.v1.operations;

import static org.apache.geode.internal.protocol.TestExecutionContext.getNoAuthCacheExecutionContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.RegionDestroyedException;
import org.apache.geode.cache.Scope;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.internal.protocol.protobuf.v1.BasicTypes;
import org.apache.geode.internal.protocol.protobuf.v1.ClientProtocol;
import org.apache.geode.internal.protocol.protobuf.v1.Failure;
import org.apache.geode.internal.protocol.protobuf.v1.MessageUtil;
import org.apache.geode.internal.protocol.protobuf.v1.RegionAPI;
import org.apache.geode.internal.protocol.protobuf.v1.Result;
import org.apache.geode.test.junit.categories.UnitTest;

@Category(UnitTest.class)
public class GetSizeRequestOperationHandlerJUnitTest extends OperationHandlerJUnitTest {
  private final String TEST_REGION1 = "test region 1";
  private Region region1Stub;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    region1Stub = mock(Region.class);
    when(region1Stub.getName()).thenReturn(TEST_REGION1);

    operationHandler = new GetSizeRequestOperationHandler();
  }

  @Test
  public void processReturnsCacheRegions() throws Exception {

    RegionAttributes regionAttributesStub = mock(RegionAttributes.class);
    when(cacheStub.getRegion(TEST_REGION1)).thenReturn(region1Stub);
    when(region1Stub.getName()).thenReturn(TEST_REGION1);
    when(region1Stub.size()).thenReturn(10);
    when(region1Stub.getAttributes()).thenReturn(regionAttributesStub);
    when(regionAttributesStub.getDataPolicy()).thenReturn(DataPolicy.PERSISTENT_REPLICATE);
    when(regionAttributesStub.getKeyConstraint()).thenReturn(String.class);
    when(regionAttributesStub.getValueConstraint()).thenReturn(Integer.class);
    when(regionAttributesStub.getScope()).thenReturn(Scope.DISTRIBUTED_ACK);


    Result result = operationHandler.process(serializationService,
        MessageUtil.makeGetSizeRequest(TEST_REGION1), getNoAuthCacheExecutionContext(cacheStub));
    RegionAPI.GetSizeResponse response = (RegionAPI.GetSizeResponse) result.getMessage();
    Assert.assertEquals(10, response.getSize());
  }

  @Test
  public void processReturnsNoCacheRegions() throws Exception {
    InternalCache emptyCache = mock(InternalCache.class);
    when(emptyCache.rootRegions())
        .thenReturn(Collections.unmodifiableSet(new HashSet<Region<String, String>>()));
    String unknownRegionName = "UNKNOWN_REGION";
    expectedException.expect(RegionDestroyedException.class);
    operationHandler.process(serializationService,
        MessageUtil.makeGetSizeRequest(unknownRegionName),
        getNoAuthCacheExecutionContext(emptyCache));
  }
}
