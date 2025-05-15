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

package org.qubership.atp.integration.configuration.feign;

import java.util.List;

import org.qubership.atp.integration.configuration.configuration.MultipartSupportConfiguration;
import org.qubership.atp.integration.configuration.model.MailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "${feign.atp.mailsender.name:atp-mail-sender}", url = "${feign.atp.mailsender.url:}",
        configuration = MultipartSupportConfiguration.class)
public interface MailSenderFeignClient {

    /**
     * Send mail to mail-sender.
     *
     * @param mail MailRequest to be sent
     * @return ResponseEntity object.
     */
    @PostMapping(value = "${feign.atp.mailsender.route}/api/v1/mail-sender/mail/send")
    ResponseEntity send(MailRequest mail);

    /**
     * Send mail with attachment to mail-sender.
     *
     * @param mail String email address
     * @param attachments List of MultipartFile attachment objects
     * @param inlines List of MultipartFile inline objects
     * @return ResponseEntity object.
     */
    @PostMapping(value = "${feign.atp.mailsender.route}/api/v1/mail-sender/mail/send",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity sendWithAttachment(@RequestPart(name = "mail") String mail,
                                      @RequestPart(name = "attachment") List<MultipartFile> attachments,
                                      @RequestPart(name = "inline") List<MultipartFile> inlines);

}
