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

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.qubership.atp.integration.configuration.helpers.KafkaAdminHelper;
import org.qubership.atp.integration.configuration.model.notification.Message;
import org.qubership.atp.integration.configuration.notification.client.KafkaNotificationClient;
import org.qubership.atp.integration.configuration.notification.client.NotificationClient;
import org.qubership.atp.integration.configuration.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableKafka
@ConditionalOnProperty(value = "atp.notification.mode", havingValue = "kafka")
public class KafkaNotificationClientConfiguration {

    /**
     * Name of Kafka Notification Topic.
     */
    @Value("${kafka.notification.topic.name}")
    private String kafkaTopic;

    /**
     * Number of partitions of Kafka Notification Topic.
     */
    @Value("${kafka.notification.topic.partitions:1}")
    private int kafkaPartitions;

    /**
     * Number of replicas of Kafka Notification Topic.
     */
    @Value("${kafka.notification.topic.replicas:3}")
    private short kafkaReplicas;

    /**
     * Kafka Producer Bootstrap Server URL.
     */
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Configure kafka admin.
     *
     * @return configured kafka admin,
     */
    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(AdminClientConfig.RETRIES_CONFIG, 3);
        return new KafkaAdmin(configs);
    }

    /**
     * Configure kafka topic via kafkaExecutionOrderPartitions and kafkaExecutionOrderReplicas.
     *
     * @return new topic.
     */
    @Bean
    public NewTopic topic() {
        return new NewTopic(kafkaTopic, kafkaPartitions, kafkaReplicas);
    }

    /**
     * Creates new producer factory.
     *
     * @return new producer factory
     */
    @Bean
    public ProducerFactory<String, Message> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Create KafkaTemplate bean.
     *
     * @param kafkaAdminHelper KafkaAdminHelper bean
     * @return new KafkaTemplate object.
     */
    @Bean
    public KafkaTemplate<String, Message> kafkaTemplate(final KafkaAdminHelper kafkaAdminHelper) {
        kafkaAdminHelper.createOrUpdateTopic(kafkaTopic, kafkaPartitions, kafkaReplicas);
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Create NotificationClient bean.
     *
     * @param kafkaTopic String name of Kafka topic
     * @param kafkaTemplate KafkaTemplate bean
     * @return new NotificationClient object.
     */
    @Bean
    public NotificationClient notificationClient(@Value("${kafka.notification.topic.name}") final String kafkaTopic,
                                                 final KafkaTemplate<String, Message> kafkaTemplate) {
        return new KafkaNotificationClient(kafkaTopic, kafkaTemplate);
    }

    /**
     * Create NotificationService bean.
     *
     * @param notificationClient NotificationClient bean
     * @return new NotificationService object.
     */
    @Bean
    public NotificationService notificationService(final NotificationClient notificationClient) {
        return new NotificationService(notificationClient);
    }

}
