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

package org.qubership.atp.integration.configuration.logging.gelf.log4j;

import java.net.UnknownHostException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.qubership.atp.integration.configuration.logging.log4j.AtpLog4jPatternLayout;

import biz.paluch.logging.gelf.log4j.GelfLogAppender;

public class FailSafeGelfLogbackAppenderLog4j extends GelfLogAppender {

    private static final String PATTERN = "%d{YYYY-MM-dd HH:mm:ss.SS} [%p] \"%t\" [%c#M] %m%n";
    private static final String INFO_MESSAGE = "Switching to fail safe console appender";
    private Logger rootLogger = Logger.getRootLogger();
    private ConsoleAppender consoleAppender;

   @Override
    public void reportError(String message, Exception exception) {
        if (exception instanceof UnknownHostException) {
            initFailSafeConsoleAppender();
        } else {
            super.reportError(message, exception);
        }
    }

    private void initFailSafeConsoleAppender() {
        AtpLog4jPatternLayout layout = new AtpLog4jPatternLayout();
        layout.setConversionPattern(PATTERN);

        consoleAppender = new ConsoleAppender(layout, ConsoleAppender.SYSTEM_OUT);
        consoleAppender.setEncoding("utf-8");
        consoleAppender.activateOptions();

        rootLogger.addAppender(consoleAppender);
        rootLogger.info(INFO_MESSAGE);
    }

    protected void append(LoggingEvent event) {
        if (event != null) {
            LoggingEvent maskedEvent = AtpLog4jPatternLayout.getMaskedLoggingEvent(event);
            super.append(maskedEvent);
        }
    }
}
