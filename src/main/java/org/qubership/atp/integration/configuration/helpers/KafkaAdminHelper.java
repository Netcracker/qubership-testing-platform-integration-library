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

package org.qubership.atp.integration.configuration.helpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaAdmin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KafkaAdminHelper {

    /**
     * KafkaAdmin bean.
     */
    private final KafkaAdmin kafkaAdmin;

    /**
     * Create or update Kafka topic with specified number of partitions and replicas.
     *
     * @param topic String topic name
     * @param partitions int number of partitions
     * @param replicas short replication factor.
     */
    public void createOrUpdateTopic(final String topic, final int partitions, final short replicas) {
        try {
            log.info("Start createOrUpdateTopic: create or update topic [name={}, partitions={}, replicationFactor={}]",
                    topic, partitions, replicas);
            AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties());
            ListTopicsResult listTopicsResult = client.listTopics();
            Set<String> existingTopics = listTopicsResult.names().get();
            if (existingTopics.contains(topic)) {
                log.debug("createOrUpdateTopic: update topic [name={}, partitions={}]", topic, partitions);
                Map<String, NewPartitions> newPartitionSet = new HashMap<>();
                newPartitionSet.put(topic, NewPartitions.increaseTo(partitions));
                client.createPartitions(newPartitionSet).all().get();
            } else {
                log.debug("createOrUpdateTopic: create new topic [name={}, partitions={}]", topic, partitions);
                client.createTopics(Collections.singleton(
                        new NewTopic(topic, partitions, replicas)),
                        new CreateTopicsOptions().timeoutMs(10000)
                ).all().get();
            }
        } catch (Exception ex) {
            log.error("Cannot create topic [name={}, partitions={}, replicas={}]", topic, partitions, replicas);
        }
    }
}
