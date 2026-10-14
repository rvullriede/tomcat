/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.jdbc.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PoolProperties.InterceptorProperty;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.apache.tomcat.jdbc.test.driver.Driver;

public class TestInterceptorResetOrder extends DefaultTestCase {

    private static final List<String> RESETS = new ArrayList<>();

    public static class RecordingInterceptor extends JdbcInterceptor {
        private String name;

        @Override
        public void setProperties(Map<String, InterceptorProperty> properties) {
            super.setProperties(properties);
            name = properties.get("name").getValue();
        }

        @Override
        public void reset(ConnectionPool parent, PooledConnection con) {
            RESETS.add(name);
        }
    }

    @Before
    public void clearResets() {
        RESETS.clear();
    }

    @Test
    public void testResetInChainOrder() throws Exception {
        datasource.setDriverClassName(Driver.class.getName());
        datasource.setUrl("jdbc:tomcat:test");
        String interceptor = RecordingInterceptor.class.getName();
        datasource.setJdbcInterceptors(
                interceptor + "(name=first);" + interceptor + "(name=second);" + interceptor + "(name=third)");
        datasource.setInitialSize(1);
        datasource.setMaxActive(1);
        datasource.setMaxIdle(1);
        datasource.setMinIdle(1);
        datasource.createPool();

        datasource.getConnection().close();
        Assert.assertEquals(Arrays.asList("first", "second", "third"), RESETS);

        RESETS.clear();
        datasource.getConnection().close();
        Assert.assertEquals(Arrays.asList("first", "second", "third"), RESETS);
    }
}
