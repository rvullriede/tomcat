/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat.jdbc.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport;
import org.apache.tomcat.jdbc.pool.interceptor.StatementCache;
import org.apache.tomcat.jdbc.pool.interceptor.StatementDecoratorInterceptor;
import org.apache.tomcat.jdbc.test.driver.Driver;

@RunWith(Parameterized.class)
public class TestStatementIdentity extends DefaultTestCase {

    private final String interceptor;

    public TestStatementIdentity(String interceptor) {
        this.interceptor = interceptor;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                { SlowQueryReport.class.getName() },
                { StatementDecoratorInterceptor.class.getName() },
                { StatementCache.class.getName() } });
    }

    @Before
    public void setUp() {
        this.datasource.setDriverClassName(Driver.class.getName());
        this.datasource.setUrl("jdbc:tomcat:test");
        // the facade would otherwise answer equals/hashCode itself
        this.datasource.setUseStatementFacade(false);
        this.datasource.setJdbcInterceptors(interceptor);
    }

    @Test
    public void testOpenStatementIdentity() throws SQLException {
        try (Connection con = this.datasource.getConnection();
                Statement statement = con.createStatement()) {
            Assert.assertEquals(statement, statement);
            Assert.assertEquals(statement.hashCode(), statement.hashCode());
            try (Statement other = con.createStatement()) {
                Assert.assertNotEquals(statement, other);
            }
        }
    }

    @Test
    public void testClosedStatementIdentity() throws SQLException {
        try (Connection con = this.datasource.getConnection()) {
            Statement statement = con.createStatement();
            int hashCode = statement.hashCode();
            statement.close();
            Assert.assertEquals(statement, statement);
            Assert.assertEquals(hashCode, statement.hashCode());
            Assert.assertNotNull(statement.toString());
            Assert.assertNotEquals(statement, null);
        }
    }

    @Test
    public void testClosedPreparedStatementIdentity() throws SQLException {
        try (Connection con = this.datasource.getConnection()) {
            PreparedStatement statement = con.prepareStatement("sql");
            int hashCode = statement.hashCode();
            statement.close();
            Assert.assertEquals(statement, statement);
            Assert.assertEquals(hashCode, statement.hashCode());
            Assert.assertNotNull(statement.toString());
        }
    }
}
