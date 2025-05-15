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

import java.util.stream.Collectors;

import org.qubership.atp.integration.configuration.metrics.providers.CustomWebMvcTagsProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsContributor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MetricsProperties.class)
public class AtpWebMetricsConfiguration {

    /**
     * Metrics Properties.
     */
    private final MetricsProperties properties;

    /**
     * Constructor from Metrics Properties.
     *
     * @param properties Metrics Properties to set.
     */
    public AtpWebMetricsConfiguration(final MetricsProperties properties) {
        this.properties = properties;
    }

    /**
     * Create Web MVC Tags Provider using contributors parameter and properties field.
     *
     * @param contributors Provider of WebMvcTagsContributor
     * @return new CustomWebMvcTagsProvider configured.
     */
    @Bean
    public CustomWebMvcTagsProvider webMvcTagsProvider(final ObjectProvider<WebMvcTagsContributor> contributors) {
        return new CustomWebMvcTagsProvider(this.properties.getWeb().getServer().getRequest().isIgnoreTrailingSlash(),
                contributors.orderedStream().collect(Collectors.toList()));
    }
}
