package com.powertester.extensionsglobal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import com.powertester.database.DBConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestRunListener implements LauncherSessionListener {
  private long testRunStartTime;
  private static final Path TEST_REPORT_PATH = Paths.get(".", "test-reports");

  /**
   * This method is called when the launcher session is opened, i.e., at the very start of the entire test run.
   * It is used to perform global setup tasks such as initializing resources that are shared across all tests.
   * Examples include starting containers, seeding databases, or initializing global fixtures.
   */
  @Override
  public void launcherSessionOpened(LauncherSession session) {
    log.info("🚀 Test run started.");

    testRunStartTime = System.currentTimeMillis();

    // Initialize DB connection pool
    DBConnection.getInstance();

    // Create test report directory if it doesn't exist
    try {
      Files.createDirectories(TEST_REPORT_PATH);
    } catch (IOException e) {
      log.error("Error creating test report directory", e);
    }
  }

  /**
   * This method is called when the launcher session is closed, i.e., at the very end of the entire test run.
   * It is used to perform global teardown tasks such as cleaning up resources that were shared across all tests.
   * Examples include stopping containers, closing database connections, or exporting test reports.
   */
  @Override
  public void launcherSessionClosed(LauncherSession session) {
    log.info("✅ Closing Hikari datasource pool (only once) at the end of the whole test run"); 
    DBConnection.getInstance().closeConnectionPool();

    log.info("⌛️ Test run completed in {} seconds.", (System.currentTimeMillis() - testRunStartTime) / 1000.0);
  }
}