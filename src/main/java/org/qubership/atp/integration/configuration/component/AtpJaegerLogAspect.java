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

package org.qubership.atp.integration.configuration.component;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.qubership.atp.integration.configuration.annotation.AtpJaegerLog;
import org.qubership.atp.integration.configuration.annotation.AtpSpanTag;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import brave.ScopedSpan;
import brave.Tracer;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AtpJaegerLogAspect {

    /**
     * Tracer object.
     */
    private final Tracer tracer;

    /**
     * Expression Parser.
     */
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * Constructor.
     *
     * @param tracer Tracer object.
     */
    public AtpJaegerLogAspect(@Autowired(required = false) final Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Additional log to jaeger.
     *
     * @param joinPoint Join Point to add span to
     * @param atpJaegerLog Jaeger Log Object
     * @return Jaeger Log Object.
     */
    @Around(value = "@annotation(atpJaegerLog)")
    public Object jaegerLog(final ProceedingJoinPoint joinPoint, final AtpJaegerLog atpJaegerLog) throws Throwable {
        Optional<ScopedSpan> nextSpan = Optional.ofNullable(tracer).map(t -> {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return t.startScopedSpan(signature.getDeclaringType().getSimpleName() + "."
                    + signature.getMethod().getName() + "()");
        });
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            nextSpan.ifPresent(span -> span.error(e));
            throw e;
        } finally {
            nextSpan.ifPresent(span -> {
                for (AtpSpanTag spanTag : atpJaegerLog.spanTags()) {
                    span.tag(spanTag.key(), getValue(joinPoint, spanTag.value()));
                }
                for (String mdcSpan : atpJaegerLog.mdcToTag()) {
                    if (MDC.get(mdcSpan) != null) {
                        span.tag(mdcSpan, MDC.get(mdcSpan));
                    }
                }
                span.finish();
            });
        }
    }

    private String getValue(final ProceedingJoinPoint joinPoint, final String argument) {
        Object[] args = joinPoint.getArgs();
        if (argument.startsWith("#")) {
            String[] parametersNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
            for (int id = 0; id < parametersNames.length; id++) {
                if (argument.startsWith("#" + parametersNames[id] + ".")
                        || argument.equals("#" + parametersNames[id])) {
                    return parseExpression(args, argument.replaceFirst("#" + parametersNames[id], "[" + id + "]"));
                }
            }
        }
        return parseExpression(args, argument);
    }

    private String parseExpression(final Object[] args, final String argument) {
        try {
            return (String) expressionParser.parseExpression(argument).getValue(args);
        } catch (Exception e) {
            return argument;
        }
    }
}
