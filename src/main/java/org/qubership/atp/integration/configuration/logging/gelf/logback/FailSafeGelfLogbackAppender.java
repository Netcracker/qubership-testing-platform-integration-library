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

import java.net.UnknownHostException;

import org.qubership.atp.crypt.CryptoTools;
import org.qubership.atp.integration.configuration.logging.logback.AtpPatternLayoutEncoder;

import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.logback.GelfLogbackAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import lombok.Data;

@Data
public class FailSafeGelfLogbackAppender extends GelfLogbackAppender {

    /**
     * Log pattern.
     */
    public static final String PATTERN
            = "%date{YYYY-MM-dd HH:mm:ss.SS} %-7([%level]) \"%thread\" %X{requestId} [%logger#%method] - %message%n";

    /**
     * Info message logged just before switching to console appender.
     */
    public static final String INFO_MESSAGE = "Switching to fail safe console appender";

    /**
     * Error message in case Gelf message couldn't send.
     */
    private static final String ERROR_CANT_SEND_GELF_MESSAGE = "Could not send GELF message";

    /**
     * Error message in case invalid Gelf message.
     */
    private static final String ERROR_INVALID_GELF_MESSAGE = "GELF Message is invalid: ";

    /**
     * Maximum length of short message.
     */
    private static final short SHORT_MESSAGE_MAX_LENGTH = 250;

    /**
     * GrayLog server available (true) or not.
     */
    private boolean graylogAvailable = true;

    /**
     * Console Appender.
     */
    private ConsoleAppender<ILoggingEvent> consoleAppender;

    /**
     * Append event to log; synchronized.
     *
     * @param eventObject ILoggingEvent to add.
     */
    @Override
    public synchronized void doAppend(final ILoggingEvent eventObject) {
        if (!graylogAvailable) {
            consoleAppender.doAppend(eventObject);
            return;
        }
        super.doAppend(eventObject);
    }

    /**
     * Report error.
     *
     * @param message to report
     * @param exception to report.
     */
    @Override
    public void reportError(final String message, final Exception exception) {
        if (exception instanceof UnknownHostException) {
            graylogAvailable = false;
            addWarn(message, exception);
            addInfo(INFO_MESSAGE);
            initFailSafeConsoleAppender();
        } else {
            super.reportError(message, exception);
        }
    }

    private void initFailSafeConsoleAppender() {
        consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(getContext());
        AtpPatternLayoutEncoder patternLayoutEncoder = new AtpPatternLayoutEncoder();
        patternLayoutEncoder.setContext(context);
        patternLayoutEncoder.setPattern(PATTERN);
        patternLayoutEncoder.start();
        consoleAppender.setEncoder(patternLayoutEncoder);
        consoleAppender.start();
    }

    /**
     * Append event to log.
     *
     * @param event ILoggingEvent to append.
     */
    @Override
    protected void append(final ILoggingEvent event) {
        if (event == null) {
            return;
        }
        try {
            GelfMessage message = this.createGelfMessage(event);
            if (!message.isValid()) {
                this.reportError(ERROR_INVALID_GELF_MESSAGE + message.toJson(), null);
                return;
            }

            String fullMsgMasked = CryptoTools.maskEncryptedData(message.getFullMessage());
            message.setFullMessage(fullMsgMasked);

            String shortMessage = fullMsgMasked.length() > SHORT_MESSAGE_MAX_LENGTH
                    ? fullMsgMasked.substring(0, SHORT_MESSAGE_MAX_LENGTH - 1)
                    : fullMsgMasked;
            message.setShortMessage(shortMessage);
            if (null == this.gelfSender || !this.gelfSender.sendMessage(message)) {
                this.reportError(ERROR_CANT_SEND_GELF_MESSAGE, null);
            }
        } catch (Exception exception) {
            this.reportError(ERROR_CANT_SEND_GELF_MESSAGE + exception.getMessage(), exception);
        }
    }
}
