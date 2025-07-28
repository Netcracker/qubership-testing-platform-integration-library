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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Aspect for processing @AuditActionRest annotations.
 * Captures method parameters and puts audit action into MDC context for REST logging.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "atp.audit.logging.rest.enable", havingValue = "true")
public class AuditRestAspect {

    public static final TemplateParserContext TEMPLATE_PARSER_CONTEXT = new TemplateParserContext("{{", "}}");
    public static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    /**
     * Update audit action for REST logging.
     * Processes @AuditActionRest annotation and sets userAction in MDC.
     */
    @Before("@annotation(org.qubership.atp.integration.configuration.configuration.AuditActionRest)")
    public void around(JoinPoint joinPoint) {
        log.debug("Capture user action for REST audit");
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            AuditActionRest annotation = signature.getMethod().getAnnotation(AuditActionRest.class);
            String[] parameterNames = signature.getParameterNames();
            EvaluationContext evaluationContext = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();

            // Set method parameters as variables for SpEL expressions
            for (int i = 0; i < args.length; ++i) {
                evaluationContext.setVariable(parameterNames[i], args[i]);
            }

            // Parse and evaluate audit action expression
            String value = EXPRESSION_PARSER.parseExpression(annotation.auditAction(), TEMPLATE_PARSER_CONTEXT)
                    .getValue(evaluationContext, String.class);

            // Store in MDC for later retrieval by AuditRestService
            MdcUtils.put("userAction", value);
        } catch (Exception e) {
            log.error("Cannot update audit action for REST logging:", e);
        }
    }
}