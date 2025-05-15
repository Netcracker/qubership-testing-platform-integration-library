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

package org.qubership.atp.integration.configuration.configuration;

import org.qubership.atp.integration.configuration.notification.client.NoneNotificationClient;
import org.qubership.atp.integration.configuration.notification.client.NotificationClient;
import org.qubership.atp.integration.configuration.service.NotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "atp.notification.mode", havingValue = "none")
public class NoneNotificationClientConfiguration {

    /**
     * Create notificationClient bean.
     *
     * @return new NoneNotificationClient object.
     */
    @Bean
    public NotificationClient notificationClient() {
        return new NoneNotificationClient();
    }

    /**
     * Create notificationService bean.
     *
     * @param notificationClient NotificationClient bean
     * @return new NotificationService object configured with notificationClient.
     */
    @Bean
    public NotificationService notificationService(final NotificationClient notificationClient) {
        return new NotificationService(notificationClient);
    }

}
