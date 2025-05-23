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

package org.qubership.atp.integration.configuration.metrics.providers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.qubership.atp.integration.configuration.mdc.MdcField;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.slf4j.MDC;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsContributor;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

public class CustomWebMvcTagsProvider implements WebMvcTagsProvider {

    /**
     * Value for unknown project id.
     */
    public static final String UNKNOWN_PROJECT = "unknown";

    /**
     * Ignore Trailing Slash mode.
     */
    private final boolean ignoreTrailingSlash;

    /**
     * List of WebMvcTagsContributors.
     */
    private final List<WebMvcTagsContributor> contributors;

    /**
     * Creates a new {@link CustomWebMvcTagsProvider} that will provide tags from the
     * given {@code contributors} in addition to its own.
     *
     * @param ignoreTrailingSlash whether trailing slashes should be ignored when determining the {@code uri} tag.
     * @param contributors the contributors that will provide additional tags
     */
    public CustomWebMvcTagsProvider(final boolean ignoreTrailingSlash,
                                    final List<WebMvcTagsContributor> contributors) {
        this.ignoreTrailingSlash = ignoreTrailingSlash;
        this.contributors = contributors;
    }

    /**
     * Populate and get tags.
     *
     * @param request HttpServletRequest to process
     * @param response HttpServletResponse to process
     * @param handler Object handler
     * @param exception Throwable exception possibly thrown during early processing
     * @return Iterable of Tags.
     */
    @Override
    public Iterable<Tag> getTags(final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 final Object handler,
                                 final Throwable exception) {
        Tags tags = addProjectIdTag(request,
                Tags.of(WebMvcTags.method(request), WebMvcTags.uri(request, response, this.ignoreTrailingSlash),
                        WebMvcTags.exception(exception), WebMvcTags.status(response), WebMvcTags.outcome(response)));
        for (WebMvcTagsContributor contributor : this.contributors) {
            tags = tags.and(contributor.getTags(request, response, handler, exception));
        }
        return tags;
    }

    /**
     * Populate and get Long Request Tags.
     *
     * @param request HttpServletRequest to process
     * @param handler Object handler
     * @return Iterable of Tags.
     */
    @Override
    public Iterable<Tag> getLongRequestTags(final HttpServletRequest request, final Object handler) {
        Tags tags = addProjectIdTag(request,
                Tags.of(WebMvcTags.method(request), WebMvcTags.uri(request, null, this.ignoreTrailingSlash)));
        for (WebMvcTagsContributor contributor : this.contributors) {
            tags = tags.and(contributor.getLongRequestTags(request, handler));
        }
        return tags;
    }

    private Tags addProjectIdTag(final HttpServletRequest request, final Tags tags) {
        String projectId = MdcUtils.getHeaderFromRequest(request,
                MdcUtils.convertIdNameToHeader(MdcField.PROJECT_ID.toString()));
        projectId = projectId == null ? MDC.get(MdcField.PROJECT_ID.toString()) : projectId;
        return tags.and(Tag.of(MdcField.PROJECT_ID.toString(), projectId == null ? UNKNOWN_PROJECT : projectId));
    }
}
