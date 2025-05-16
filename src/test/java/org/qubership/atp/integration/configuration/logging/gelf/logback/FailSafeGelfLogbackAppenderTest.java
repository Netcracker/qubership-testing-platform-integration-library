/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.integration.configuration.logging.gelf.logback;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class FailSafeGelfLogbackAppenderTest {

    /**
     * Fail Safe Gelf Logback Appender configured with unknown Host.
     */
    private FailSafeGelfLogbackAppender failSafeGelfLogbackAppenderHostUnknown;

    /**
     * Logger with correct configuration.
     */
    private static org.slf4j.Logger log = LoggerFactory.getLogger(FailSafeGelfLogbackAppenderTest.class);

    /**
     * Logger name.
     */
    private static final String LOGGER_NAME = "org.qubership.junit.log";

    /**
     * Logger configured with wrong configured appender.
     */
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(LOGGER_NAME);

    /**
     * Test message to log.
     */
    private static final String MSG = "Test message";

    /**
     * Incorrect host error string.
     */
    private static final String INCORRECT_HOST = "incorrect_host";

    /**
     * Warning message.
     */
    private static final String WARNING_MESSAGE_INCORRECT_HOST = "Unknown GELF server hostname:" + INCORRECT_HOST;

    /**
     * Configure and add appender, then start it before tests.
     */
    @Before
    public void setup() {
        failSafeGelfLogbackAppenderHostUnknown = new FailSafeGelfLogbackAppender();
        failSafeGelfLogbackAppenderHostUnknown.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        failSafeGelfLogbackAppenderHostUnknown.setHost(INCORRECT_HOST);
        LOGGER.addAppender(failSafeGelfLogbackAppenderHostUnknown);
        failSafeGelfLogbackAppenderHostUnknown.start();
    }

    /**
     * Stop and detach appender after tests.
     */
    @After
    public void cleanUp() {
        failSafeGelfLogbackAppenderHostUnknown.stop();
        LOGGER.detachAppender(failSafeGelfLogbackAppenderHostUnknown);
    }

    /**
     * Test of Console appender creation in case Host is incorrect.
     */
    @Test
    public void generateLogsWithAppenderHostIncorrectExpectCreatingConsoleAppender() {
        generateLogs(MSG);

        Assert.assertFalse(failSafeGelfLogbackAppenderHostUnknown.isGraylogAvailable());
        Assert.assertTrue(failSafeGelfLogbackAppenderHostUnknown.getContext()
                .getStatusManager()
                .getCopyOfStatusList()
                .stream()
                .anyMatch(status -> status instanceof WarnStatus
                        && WARNING_MESSAGE_INCORRECT_HOST.equals(status.getMessage())));
        Assert.assertTrue(failSafeGelfLogbackAppenderHostUnknown.getContext()
                .getStatusManager()
                .getCopyOfStatusList()
                .stream()
                .anyMatch(status -> status instanceof InfoStatus
                        && FailSafeGelfLogbackAppender.INFO_MESSAGE.equals(status.getMessage())));
        Assert.assertNotNull(failSafeGelfLogbackAppenderHostUnknown.getConsoleAppender());
    }

    private void generateLogs(final String message) {
        log.info(message);
    }

}
