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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.kafka.common.errors.RecordTooLargeException;
import org.qubership.atp.integration.configuration.feign.MailSenderFeignClient;
import org.qubership.atp.integration.configuration.model.MailRequest;
import org.qubership.atp.integration.configuration.model.MailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MailSenderService {

    private final KafkaTemplate<UUID, MailRequest> kafkaTemplate;
    private final MailSenderFeignClient mailSenderFeignClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kafka.mails.topic:ci_mails}")
    private String mailRequestTopic;
    @Value("${spring.application.name}")
    private String serviceName;


    /**
     * Sends mail via kafka, if possible, or via rest.
     *
     * @param mail mail request parameters
     * @return result of sending mail
     */
    public MailResponse send(MailRequest mail) {
        if (StringUtils.isEmpty(mail.getService())) {
            mail.setService(serviceName);
        }
        if (kafkaTemplate == null) {
            return sendViaRest(mail);
        }
        return sendViaKafka(mail);
    }

    /**
     * Sends mail with attachments.
     *
     * @param mail mail request parameters
     * @param attachments attached non-inline files
     * @return result of sending mail
     */
    public MailResponse send(MailRequest mail, List<MultipartFile> attachments) {
        if (StringUtils.isEmpty(mail.getService())) {
            mail.setService(serviceName);
        }
        return sendViaRest(mail, attachments, null);
    }

    /**
     * Sends mail with inline and non-inline attachments.
     *
     * @param mail mail request parameters
     * @param attachments attached non-inlines files
     * @param inlines attached inlines files
     * @return result of sending mail
     */
    public MailResponse send(MailRequest mail, List<MultipartFile> attachments, List<MultipartFile> inlines) {
        if (StringUtils.isEmpty(mail.getService())) {
            mail.setService(serviceName);
        }
        return sendViaRest(mail, attachments, inlines);
    }

    /**
     * Sends mail with inline attachments.
     *
     * @param mail mail request parameters
     * @param inlines attachment inline files
     * @return result of sending mail
     */
    public MailResponse sendWithInline(MailRequest mail, List<MultipartFile> inlines) {
        if (StringUtils.isEmpty(mail.getService())) {
            mail.setService(serviceName);
        }
        return sendViaRest(mail, null, inlines);
    }

    /**
     * Sends mail via rest.
     *
     * @param mail mail request parameters
     * @return result of sending mail
     */
    private MailResponse sendViaRest(MailRequest mail) {
        MailResponse mailResponse = new MailResponse();
        try {
            ResponseEntity response = mailSenderFeignClient.send(mail);
            mailResponse.setStatus(response.getStatusCodeValue());
            mailResponse.setMessage("Mail sent successfully");
        } catch (Exception ex) {
            log.error("Failed to send mail", ex);
            mailResponse = getMailResponse(ex);
        }
        return mailResponse;
    }

    /**
     * Sends mail with attachments via rest.
     *
     * @param mail mail request parameters
     * @param attachments attached non-inline files
     * @param inlines attached inline files
     * @return result of sending mail
     */
    private MailResponse sendViaRest(MailRequest mail, List<MultipartFile> attachments, List<MultipartFile> inlines) {
        MailResponse mailResponse = new MailResponse();
        try {
            ResponseEntity response = mailSenderFeignClient.sendWithAttachment(
                    objectMapper.writeValueAsString(mail), attachments, inlines);
            mailResponse.setStatus(response.getStatusCodeValue());
            mailResponse.setMessage("Mail sent successfully");
        } catch (Exception ex) {
            log.error("Failed to send mail", ex);
            mailResponse = getMailResponse(ex);
        }
        return mailResponse;
    }

    private MailResponse getMailResponse(Exception ex) {
        MailResponse mailResponse = new MailResponse();
        String errorMessage = ex.toString();
        String errorMessageBody = errorMessage.substring(errorMessage.indexOf("{"));
        if (!StringUtils.isEmpty(errorMessageBody)) {
            try {
                mailResponse = objectMapper.readValue(errorMessageBody, MailResponse.class);
            } catch (JsonProcessingException e) {
                log.warn("Can't cast exception message to MailResponse type: " + e);
                mailResponse.setStatus(500);
                mailResponse.setMessage("Failed to send mail. Undefined error");
                mailResponse.setTimestamp(new Date());
                mailResponse.setTrace(Arrays.toString(ex.getStackTrace()));
            }
        }
        return mailResponse;
    }

    /**
     * Sends mail via kafka.
     *
     * @param mail mail request parameters
     * @return result of sending request to kafka. If you want to get the result of an email, you need a KafkaListener
     */
    private MailResponse sendViaKafka(MailRequest mail) {
        MailResponse response = new MailResponse();
        response.setTimestamp(new Date());
        try {
            ListenableFuture<SendResult<UUID, MailRequest>> future = kafkaTemplate.send(mailRequestTopic, mail);
            future.get();
            response.setStatus(200);
            response.setMessage("Mail request successfully sent to kafka");
        } catch (RecordTooLargeException ex) {
            log.warn("The mail is too big to send to kafka. Send it via REST");
            return sendViaRest(mail);
        } catch (Exception ex) {
            log.error("Failed to send mail to kafka topic", ex);
            response.setStatus(500);
            response.setMessage(ex.getMessage());
        }
        return response;
    }

}
