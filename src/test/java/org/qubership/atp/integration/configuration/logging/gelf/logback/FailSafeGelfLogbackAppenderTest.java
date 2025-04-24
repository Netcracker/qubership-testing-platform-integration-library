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

    private FailSafeGelfLogbackAppender failSafeGelfLogbackAppenderHostUnknown;

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FailSafeGelfLogbackAppenderTest.class);
    private static final String LOGGER_NAME = "org.qubership.junit.log";
    private static final Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);

    private static final String MSG = "Test message";
    private static final String INCORRECT_HOST = "incorrect_host";
    private static final String WARNING_MESSAGE_INCORRECT_HOST = "Unknown GELF server hostname:" + INCORRECT_HOST;

    @Before
    public void setup() {
        failSafeGelfLogbackAppenderHostUnknown = new FailSafeGelfLogbackAppender();
        failSafeGelfLogbackAppenderHostUnknown.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        failSafeGelfLogbackAppenderHostUnknown.setHost(INCORRECT_HOST);
        logger.addAppender(failSafeGelfLogbackAppenderHostUnknown);
        failSafeGelfLogbackAppenderHostUnknown.start();
    }

    @After
    public void cleanUp() {
        failSafeGelfLogbackAppenderHostUnknown.stop();
        logger.detachAppender(failSafeGelfLogbackAppenderHostUnknown);
    }

    @Test
    public void generateLogsWithAppender_HostIncorrect_ExpectCreatingConsoleAppender() {
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

    private void generateLogs(String message){
        log.info(message);
    }

}
