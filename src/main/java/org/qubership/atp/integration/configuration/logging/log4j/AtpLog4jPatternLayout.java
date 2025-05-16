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

package org.qubership.atp.integration.configuration.logging.log4j;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.qubership.atp.crypt.CryptoTools;

public class AtpLog4jPatternLayout extends PatternLayout {

    /**
     * Format LoggingEvent into String.
     *
     * @param event - logging event
     * @return - formatted logging event.
     */
    @Override
    public String format(final LoggingEvent event) {
        if (event.getMessage() instanceof String) {
            LoggingEvent maskedEvent = getMaskedLoggingEvent(event);
            return super.format(maskedEvent);
        }
        return super.format(event);
    }

    /**
     * Creates masked logging event.
     *
     * @param event - logging event
     * @return - masked logging event
     */
    public static LoggingEvent getMaskedLoggingEvent(final LoggingEvent event) {
        String message = event.getRenderedMessage();

        String maskedMessage = CryptoTools.maskEncryptedData(message);
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
        Throwable throwable = event.getThrowableInformation() != null
                ? event.getThrowableInformation().getThrowable() : null;
        return new LoggingEvent(event.fqnOfCategoryClass,
                Logger.getLogger(event.getLoggerName()), event.timeStamp,
                event.getLevel(), maskedMessage, throwable);
    }
}
