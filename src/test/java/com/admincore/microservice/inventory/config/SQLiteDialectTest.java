package com.admincore.microservice.inventory.config;

import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteDialectTest {

    @Test
    void testDialectInitialization() {
        SQLiteDialect dialect = new SQLiteDialect();
        assertNotNull(dialect);
    }

    @Test
    void testIdentityColumnSupport() {
        SQLiteDialect dialect = new SQLiteDialect();
        IdentityColumnSupport identitySupport = dialect.getIdentityColumnSupport();

        assertNotNull(identitySupport);
        assertTrue(identitySupport.supportsIdentityColumns());
        assertEquals("select last_insert_rowid()", identitySupport.getIdentitySelectString(null, null, 0));
        assertEquals("integer", identitySupport.getIdentityColumnString(0));
    }

    @Test
    void testSupportsMethods() {
        SQLiteDialect dialect = new SQLiteDialect();

        assertTrue(dialect.supportsTemporaryTables());
        assertTrue(dialect.supportsIfExistsBeforeTableName());
        assertFalse(dialect.supportsCascadeDelete());
        assertEquals("", dialect.getDropForeignKeyString());
    }
}