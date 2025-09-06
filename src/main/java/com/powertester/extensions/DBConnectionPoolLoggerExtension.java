package com.powertester.extensions;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.powertester.database.DBConnection;

public class DBConnectionPoolLoggerExtension implements AfterEachCallback {
    @Override
    public void afterEach(ExtensionContext context) {
        DBConnection.getInstance().logConnectionPoolStatus();
    }
}
