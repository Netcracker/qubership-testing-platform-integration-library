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
import java.util.UUID;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.qubership.atp.integration.configuration.model.KafkaMailResponse;
import org.qubership.atp.integration.configuration.model.MailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@ConditionalOnProperty(name = "kafka.mails.enable")
public class KafkaMailSenderConfiguration {

    /**
     * Name of Kafka Topic.
     */
    @Value("${kafka.mails.topic:ci_mails}")
    private String kafkaMailsTopic;

    /**
     * Number of Kafka Topic partitions.
     */
    @Value("${kafka.mails.topic.partitions:1}")
    private int kafkaMailsPartitions;

    /**
     * Number of Kafka Topic replicas.
     */
    @Value("${kafka.mails.topic.replicas:3}")
    private short kafkaMailsReplicas;

    /**
     * Maximum message size (in bytes).
     */
    @Value("${kafka.mails.message.size:15728640}")
    public int messageSize;

    /**
     * Compression type.
     */
    @Value("${kafka.mails.compression.type:lz4}")
    public String compressionType;

    /**
     * Kafka Producer Bootstrap Server URL.
     */
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String kafkaServers;

    /**
     * Responses Group Id.
     */
    @Value("${kafka.mails.responses.group.id}")
    private String groupId;

    /**
     * Create or update topic for mail request.
     *
     * @return NewTopic configured with kafkaMailsTopic, kafkaMailsPartitions and kafkaMailsReplicas configuration
     * settings.
     */
    @Bean
    public NewTopic mailsTopic() {
        return TopicBuilder.name(kafkaMailsTopic)
                .partitions(kafkaMailsPartitions)
                .replicas(kafkaMailsReplicas)
                .build();
    }

    /**
     * Create new KafkaTemplate.
     *
     * @return new KafkaTemplate configured with producerConfig().
     */
    @Bean
    public KafkaTemplate<UUID, MailRequest> kafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfig()));
    }

    /**
     * Create factory for KafkaListener for mail responses.
     *
     * @return ConcurrentKafkaListenerContainerFactory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<UUID, KafkaMailResponse> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<UUID, KafkaMailResponse> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfig(),
                new UUIDDeserializer(), new JsonDeserializer<>(KafkaMailResponse.class)));
        return factory;
    }

    private Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, messageSize);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        return props;
    }

    private Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return props;
    }

}
