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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.micrometer.core.instrument.Tag;

public class CustomWebMvcTagsProviderTest {
    private CustomWebMvcTagsProvider provider;
    private HttpServletRequestWrapper request;
    private HttpServletResponseWrapper response;

    /**
     * Before tests handler.
     */
    @Before
    public void init() {
        provider = new CustomWebMvcTagsProvider(true, new ArrayList<>());
        request = mock(HttpServletRequestWrapper.class);
        response = mock(HttpServletResponseWrapper.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singletonList("x-project-id")));
        when(response.getStatus()).thenReturn(200);
    }

    /**
     * Test of getTags() for specified project.
     */
    @Test
    public void testGetTags_headersHaveProjectId_ReturnTagsWithProjectId() {
        checkProjectId("5c043ea3-583d-4887-b0f3-bd46f2e7d76f", false);
    }

    /**
     * Test of getTags() for unknown project.
     */
    @Test
    public void testGetTags_headersHaveNotProjectId_ReturnTagsWithProjectIdIsUnknown() {
        checkProjectId("unknown", false);
    }

    /**
     * Test of getLongRequestTags() for specified project.
     */
    @Test
    public void testLongRequestTags_headersHaveProjectId_ReturnTagsWithProjectId() {
        checkProjectId("5c043ea3-583d-4887-b0f3-bd46f2e7d76f", true);
    }

    /**
     * Test of getLongRequestTags() for unknown project.
     */
    @Test
    public void testGetLongRequestTags_headersHaveNotProjectId_ReturnTagsWithProjectIdIsUnknown() {
        checkProjectId("unknown", true);
    }

    private void checkProjectId(String expectedProjectId, boolean isGetLong) {
        when(request.getHeader(any())).thenReturn(expectedProjectId);
        Iterable<Tag> tags = isGetLong
                ? provider.getLongRequestTags(request,  null)
                : provider.getTags(request, response, null, null);
        for (Tag tag : tags) {
            if (tag.getKey().equals("projectId")) {
                Assert.assertEquals(expectedProjectId, tag.getValue());
                return;
            }
        }
        Assert.fail();
    }

}
