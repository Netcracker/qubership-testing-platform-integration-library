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
    public static final String PATTERN
            = "%date{YYYY-MM-dd HH:mm:ss.SS} %-7([%level]) \"%thread\" %X{requestId} [%logger#%method] - %message%n";

    public static final String INFO_MESSAGE = "Switching to fail safe console appender";

    private boolean graylogAvailable = true;
    private ConsoleAppender<ILoggingEvent> consoleAppender;

    @Override
    public synchronized void doAppend(ILoggingEvent eventObject) {
        if (!graylogAvailable) {
            consoleAppender.doAppend(eventObject);
            return;
        }
        super.doAppend(eventObject);
    }

    @Override
    public void reportError(String message, Exception exception) {
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

    @Override
    protected void append(ILoggingEvent event) {
        if (event != null) {
            try {
                GelfMessage message = this.createGelfMessage(event);
                if (!message.isValid()) {
                    this.reportError("GELF Message is invalid: " + message.toJson(), (Exception) null);
                    return;
                }

                String fullMsgMasked = CryptoTools.maskEncryptedData(message.getFullMessage());
                message.setFullMessage(fullMsgMasked);

                String shortMessage = fullMsgMasked.length() > 250 ? fullMsgMasked.substring(0, 249) : fullMsgMasked;
                message.setShortMessage(shortMessage);
                if (null == this.gelfSender || !this.gelfSender.sendMessage(message)) {
                    this.reportError("Could not send GELF message", (Exception) null);
                }
            } catch (Exception var3) {
                this.reportError("Could not send GELF message: " + var3.getMessage(), var3);
            }
        }
    }
}
