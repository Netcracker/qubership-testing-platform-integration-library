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

    /**
     * Kafka Notification Topic Name.
     */
    @Value("${kafka.notification.topic.name}")
    private final String topicName;

    /**
     * KafkaTemplate object.
     */
    private final KafkaTemplate<String, Message> kafkaTemplate;

    /**
     * Sends the notification to kafka.
     *
     * @param notification notification to be sent.
     */
    public void sendNotification(Notification notification) {
        Message message = new Message(notification);
        try {
            log.info("Send notification to kafka");
            kafkaTemplate.send(topicName, message);
        } catch (Exception e) {
            log.error("Sending of notification message to kafka is failed", e);
        }
    }

    /**
     * Sends the list of notifications to kafka.
     *
     * @param notifications list of notifications to be sent.
     */
    public void sendNotifications(List<Notification> notifications) {
        log.info("Send notifications. Count: {}", notifications.size());
        for (Notification notification : notifications) {
            sendNotification(notification);
        }
    }

}
