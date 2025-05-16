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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.qubership.atp.integration.configuration.feign.MailSenderFeignClient;
import org.qubership.atp.integration.configuration.model.MailRequest;
import org.qubership.atp.integration.configuration.model.MailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MailSenderServiceTest {

    /**
     * MailSenderFeignClient bean.
     */
    @MockBean
    private MailSenderFeignClient mailSenderFeignClient;

    /**
     * KafkaTemplate bean.
     */
    @MockBean
    private KafkaTemplate<UUID, MailRequest> kafkaTemplate;

    /**
     * MailSenderService bean.
     */
    @Autowired
    private MailSenderService mailSenderService;

    /**
     * Test sending mail.
     */
    @Test
    public void sendMail() {
        MailRequest request = new MailRequest();
        request.setService("test");
        Mockito.when(mailSenderFeignClient.send(request)).thenReturn(new ResponseEntity(HttpStatus.OK));
        MailResponse response = mailSenderService.send(request);
        Assert.assertEquals(200, response.getStatus());
        Mockito.verify(mailSenderFeignClient).send(request);

        MailRequest otherRequest = new MailRequest();
        otherRequest.setService("test");
        MailResponse expectedResponse = new MailResponse();
        expectedResponse.setStatus(500);
        expectedResponse.setMessage("some error");
        Mockito.doThrow(new RuntimeException("{\"status\":500, \"message\":\"some error\"}"))
                .when(mailSenderFeignClient).send(otherRequest);
        response = mailSenderService.send(request);
        Assert.assertEquals(expectedResponse, response);

        ReflectionTestUtils.setField(mailSenderService, "kafkaTemplate", kafkaTemplate);
        mailSenderService.send(request);
        Mockito.verify(kafkaTemplate).send("ci_mails", request);
    }

    /**
     * Test sending mail with attachments.
     *
     * @throws JsonProcessingException in case ObjectMapper exceptions.
     */
    @Test
    public void sendMailWithNonInlineAttachment() throws JsonProcessingException {
        MailRequest request = new MailRequest();
        request.setService("test");
        MultipartFile attachment = new MockMultipartFile("test", new byte[]{});
        List<MultipartFile> attachments = Arrays.asList(attachment);
        ObjectMapper mapper = new ObjectMapper();
        Mockito.when(mailSenderFeignClient.sendWithAttachment(mapper.writeValueAsString(request), attachments, null))
                .thenReturn(new ResponseEntity(HttpStatus.OK));
        mailSenderService.send(request, attachments);
        Mockito.verify(mailSenderFeignClient)
                .sendWithAttachment(mapper.writeValueAsString(request), attachments, null);
    }

    /**
     * Test sending mail with attachments and inline attachments.
     *
     * @throws JsonProcessingException in case ObjectMapper exceptions.
     */
    @Test
    public void sendMailWithNonInlineAndInlineAttachment() throws JsonProcessingException {
        MailRequest request = new MailRequest();
        request.setService("test");
        MultipartFile attachment = new MockMultipartFile("test1", new byte[]{});
        List<MultipartFile> attachments = Arrays.asList(attachment);
        MultipartFile inline = new MockMultipartFile("test2", new byte[]{});
        List<MultipartFile> inlines = Arrays.asList(inline);
        ObjectMapper mapper = new ObjectMapper();
        Mockito.when(mailSenderFeignClient.sendWithAttachment(mapper.writeValueAsString(request), attachments, inlines))
                .thenReturn(new ResponseEntity(HttpStatus.OK));
        mailSenderService.send(request, attachments, inlines);
        Mockito.verify(mailSenderFeignClient)
                .sendWithAttachment(mapper.writeValueAsString(request), attachments, inlines);
    }
}
