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

package org.qubership.atp.integration.configuration.notification.client;

import java.util.Arrays;
import java.util.List;

import org.qubership.atp.integration.configuration.feign.NotificationFeignClient;
import org.qubership.atp.integration.configuration.model.notification.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RestNotificationClient implements NotificationClient {

    private final NotificationFeignClient notificationFeignClient;

    /**
     * Sends the notification to atp-notification via REST.
     *
     * @param notification notification that need to send
     */
    public void sendNotification(Notification notification) {
        try {
            log.info("Send notifications via REST");
            notificationFeignClient.sendNotifications(Arrays.asList(notification));
        } catch (Exception e) {
            log.error("Send notification message to atp-notification failed", e);
        }
    }

    /**
     * Sends the list of notifications to atp-notification via REST.
     *
     * @param notifications list of notifications that need to send
     */
    public void sendNotifications(List<Notification> notifications) {
        try {
            log.info("Send notifications. Count:{}", notifications.size());
            notificationFeignClient.sendNotifications(notifications);
        } catch (Exception e) {
            log.error("Send notification message to atp-notification failed", e);
        }
    }

}
