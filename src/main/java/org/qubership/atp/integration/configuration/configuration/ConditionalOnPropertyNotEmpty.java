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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(ConditionalOnPropertyNotEmpty.OnPropertyNotEmptyCondition.class)
public @interface ConditionalOnPropertyNotEmpty {

    /**
     * Get mentioned property value.
     *
     * @return String value of the property.
     */
    String value();

    class OnPropertyNotEmptyCondition implements Condition {

        /**
         * Check if value is not empty.
         *
         * @param context  the condition context
         * @param metadata the metadata of the {@link org.springframework.core.type.AnnotationMetadata class}
         *                 or {@link org.springframework.core.type.MethodMetadata method} being checked
         * @return true if the value is not null and trimmed value is not empty; otherwise false.
         */
        @Override
        public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalOnPropertyNotEmpty.class.getName());
            if (Objects.isNull(attrs)) {
                return false;
            }
            String propertyName = (String) attrs.get("value");
            String val = context.getEnvironment().getProperty(propertyName);
            return val != null && !val.trim().isEmpty();
        }
    }
}
