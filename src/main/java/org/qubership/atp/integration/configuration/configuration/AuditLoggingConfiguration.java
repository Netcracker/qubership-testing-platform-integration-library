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

import java.util.Properties;
import java.util.UUID;

import javax.servlet.Filter;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.qubership.atp.integration.configuration.filters.AuditLoggingFilter;
import org.qubership.atp.integration.configuration.helpers.HttpRequestParseHelper;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.helpers.KafkaAdminHelper;
import org.qubership.atp.integration.configuration.protos.KafkaAuditLoggingMessage.AuditLoggingMessage;
import org.qubership.atp.integration.configuration.serializers.KafkaProtobufSerializer;
import org.qubership.atp.integration.configuration.service.AuditLoggingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@ConditionalOnProperty(value = "atp.audit.logging.enable", havingValue = "true")
@EnableAspectJAutoProxy
@ComponentScan("org.qubership.atp.integration.configuration")
@ComponentScan("org.qubership.atp.integration.configuration.*")
public class AuditLoggingConfiguration {

    /**
     * Topic name for audit logging configuration setting.
     */
    @Value("${atp.audit.logging.topic.name}")
    private String topic;

    /**
     * Number of audit logging Topic partitions configuration setting.
     */
    @Value("${atp.audit.logging.topic.partitions:1}")
    private int partitions;

    /**
     * Number of audit logging Topic replicas configuration setting.
     */
    @Value("${atp.audit.logging.topic.replicas:3}")
    private short replicas;

    /**
     * Kafka Producer Bootstrap Server URL for reporting.
     */
    @Value("${atp.reporting.kafka.producer.bootstrap-server}")
    private String bootstrapServers;

    /**
     * Create and configure Kafka audit logging producer.
     * Also, create or update topic according configuration settings.
     *
     * @param kafkaAdminHelper bean
     * @return new KafkaProducer configured.
     */
    @Bean
    public KafkaProducer<UUID, AuditLoggingMessage> auditLoggingKafkaProducer(final KafkaAdminHelper kafkaAdminHelper) {
        kafkaAdminHelper.createOrUpdateTopic(topic, partitions, replicas);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("key.serializer", UUIDSerializer.class.getName());
        properties.setProperty("value.serializer", KafkaProtobufSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }

    /**
     * Create auditLoggingService bean.
     *
     * @param producer Audit Logging Message producer
     * @param jwtHelper JwtParseHelper bean
     * @param requestHelper HttpRequestParseHelper bean
     * @return new AuditLoggingService bean constructed with parameters given.
     */
    @Bean
    public AuditLoggingService auditLoggingService(final Producer<UUID, AuditLoggingMessage> producer,
                                                   final JwtParseHelper jwtHelper,
                                                   final HttpRequestParseHelper requestHelper) {
        return new AuditLoggingService(producer, jwtHelper, requestHelper);
    }

    /**
     * Create new Filter bean.
     *
     * @param auditLoggingService AuditLoggingService bean
     * @param jwtHelper JwtParseHelper bean
     * @return new AuditLoggingFilter bean constructed with auditLoggingService and jwtHelper parameters.
     */
    @Bean
    public Filter auditLoggingFilter(final AuditLoggingService auditLoggingService,
                                     final JwtParseHelper jwtHelper) {
        return new AuditLoggingFilter(auditLoggingService, jwtHelper);
    }
}
