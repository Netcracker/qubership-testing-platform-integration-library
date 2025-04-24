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

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "atp.audit.logging.enable", havingValue = "true")
public class AuditAspect {

    public static final TemplateParserContext TEMPLATE_PARSER_CONTEXT = new TemplateParserContext("{{", "}}");
    public static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    /**
     * Update audit action.
     */
    @Before("@annotation(org.qubership.atp.integration.configuration.configuration.AuditAction)")
    public void around(JoinPoint joinPoint) {
        log.debug("Capture user action");
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            AuditAction annotation = (AuditAction) signature.getMethod().getAnnotation(AuditAction.class);
            String[] parameterNames = signature.getParameterNames();
            EvaluationContext evaluationContext = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < args.length; ++i) {
                evaluationContext.setVariable(parameterNames[i], args[i]);
            }
            String value = EXPRESSION_PARSER.parseExpression(annotation.auditAction(), TEMPLATE_PARSER_CONTEXT)
                    .getValue(evaluationContext, String.class);
            MdcUtils.put("userAction", value);
        } catch (Exception e) {
            log.error("Can not update audit action trace:", e);
        }
    }
}
