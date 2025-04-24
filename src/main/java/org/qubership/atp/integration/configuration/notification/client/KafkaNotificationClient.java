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

import java.util.List;

import org.qubership.atp.integration.configuration.model.notification.Message;
import org.qubership.atp.integration.configuration.model.notification.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KafkaNotificationClient implements NotificationClient {

    @Value("${kafka.notification.topic.name}")
    private final String topicName;
    private final KafkaTemplate<String, Message> kafkaTemplate;

    /**
     * Sends the notification to kafka.
     *
     * @param notification notification that need to send
     */
    public void sendNotification(Notification notification) {
        Message message = new Message(notification);
        try {
            log.info("Send notification to kafka");
            kafkaTemplate.send(topicName, message);
        } catch (Exception e) {
            log.error("Send notification message to kafka failed", e);
        }
    }

    /**
     * Sends the list of notifications to kafka.
     *
     * @param notifications list of notifications that need to send
     */
    public void sendNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            Message message = new Message(notification);
            try {
                log.info("Send notifications. Count:{}", notifications.size());
                kafkaTemplate.send(topicName, message);
            } catch (Exception e) {
                log.error("Send notification message to kafka failed", e);
            }
        }
    }

}
