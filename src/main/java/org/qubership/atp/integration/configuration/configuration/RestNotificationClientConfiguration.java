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

import org.qubership.atp.integration.configuration.feign.NotificationFeignClient;
import org.qubership.atp.integration.configuration.notification.client.NotificationClient;
import org.qubership.atp.integration.configuration.notification.client.RestNotificationClient;
import org.qubership.atp.integration.configuration.service.NotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "atp.notification.mode", havingValue = "rest", matchIfMissing = true)
public class RestNotificationClientConfiguration {

    /**
     * Create notificationClient bean.
     *
     * @param notificationFeignClient NotificationFeignClient bean
     * @return new RestNotificationClient object configured with notificationFeignClient.
     */
    @Bean
    public NotificationClient notificationClient(final NotificationFeignClient notificationFeignClient) {
        return new RestNotificationClient(notificationFeignClient);
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
