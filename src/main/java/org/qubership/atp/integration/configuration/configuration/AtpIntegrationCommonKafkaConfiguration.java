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
import org.qubership.atp.integration.configuration.helpers.KafkaAdminHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaAdmin;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@ConditionalOnPropertyNotEmpty("spring.kafka.producer.bootstrap-servers")
public class AtpIntegrationCommonKafkaConfiguration {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Configure kafka admin.
     *
     * @return configured kafka admin.
     */
    @Bean
    @Lazy
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(AdminClientConfig.RETRIES_CONFIG, 3);
        return new KafkaAdmin(configs);
    }

    /**
     * Configure kafka admin helper.
     *
     * @param kafkaAdmin object
     * @return configured kafka admin helper.
     */
    @Bean
    public KafkaAdminHelper kafkaAdminHelper(final KafkaAdmin kafkaAdmin) {
        return new KafkaAdminHelper(kafkaAdmin);
    }
}
