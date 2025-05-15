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

import org.qubership.atp.integration.configuration.model.AtpRoute;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "${feign.atp.public.gateway.name:atp-public-gateway}",
        url = "${atp.public.gateway.url:}")
public interface PublicGatewayFeignClient {

    /**
     * Register AtpRoute in the public gateway.
     *
     * @param atpRoute AtpRoute object to register.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "/register")
    void register(@RequestBody AtpRoute atpRoute);
}
