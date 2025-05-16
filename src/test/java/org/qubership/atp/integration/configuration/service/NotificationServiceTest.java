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

package org.qubership.atp.integration.configuration.service;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.qubership.atp.integration.configuration.feign.NotificationFeignClient;
import org.qubership.atp.integration.configuration.model.notification.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "atp.notification.mode=rest"
})
public class NotificationServiceTest {

    /**
     * NotificationFeignClient bean.
     */
    @MockBean
    NotificationFeignClient notificationFeignClient;

    /**
     * NotificationService bean.
     */
    @Autowired
    NotificationService notificationService;

    /**
     * Test of notification sending.
     */
    @Test
    public void sendNotificationTest() {
        Notification notification = new Notification("test message1", UUID.randomUUID());
        notificationService.sendNotification(notification);
    }

}
