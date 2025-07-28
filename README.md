# atp-integration-spring-boot-starter

## Purpose

The library is for integration of ATP2 with Registry service, Public gateway service and Internal Gateway service.

## How to add

### How to add into SpringBoot application
#### 1. Add dependency

```xml
<dependency>
    <groupId>org.qubership.atp</groupId>
    <artifactId>atp-integration-spring-boot-starter</artifactId>
    <version>0.2.38</version>
</dependency>
```

#### 2. Add annotation into Main class
```text
@EnableDiscoveryClient
```

#### 3. Add properties into application.properties

- Service name:
```properties
spring.application.name=atp-service-name
```

- Set 'atp.service.public' property to true in order to register the service in the public gateway
```properties
atp.service.public=true
```

- Set 'atp.service.internal' property to true in order to register the service in the internal gateway
```properties
atp.service.internal=true
```

- URL of registry service:
```properties
eureka.client.serviceUrl.defaultZone= http://atp-registry-service:8761/eureka/
```

- Path-to-service (If empty or absent, default path will be registered: 'api/service-name/v1')
```properties
atp.service.path=/api/atp-service-name/v1/**
```

- To start the service without registering in the registry, one should set:
```properties
eureka.client.enabled=false
```

```properties
eureka.instance.preferIpAddress=true
```

## FeignClient and RestController Logging
### 1. Add properties into application.properties
```properties
atp.logging.controller.headers=${ATP_HTTP_LOGGING_HEADERS:true}
atp.logging.controller.headers.ignore=${ATP_HTTP_LOGGING_HEADERS_IGNORE:}
atp.logging.controller.uri.ignore=${ATP_HTTP_LOGGING_URI_IGNORE:/deployment/readiness /deployment/liveness}
```

* By default, _atp.logging.controller.headers_ and _atp.logging.feignclient.headers_ are false.
* _atp.logging.controller.headers_ - To log request/response headers for RestController.
* _atp.logging.controller.headers.ignore_ - To ignore specified headers while logging for RestController. Tokens should be separated with spaces.
* _atp.logging.controller.uri.ignore_ - To ignore specified endpoints while logging.
* Properties _atp.logging.controller.headers.ignore_ and _atp.logging.controller.uri.ignore_ support regular expressions.

### 2. Add configuration into logback.xml
```xml
    <if condition='${ATP_HTTP_LOGGING}'>
        <then>
            <logger name="org.qubership.atp.common.logging.filter.LoggingFilter" level="DEBUG" additivity="false">
                <appender-ref ref="ASYNC_GELF"/>
            </logger>
        </then>
    </if>
```

To turn logging ON at local machine, one should add options into JVM parameters:
```bash
-Dlogging.level.org.qubership.atp.common.logging.filter.LoggingFilter=debug
```

## The configuration to use Notification client

### Add annotation into Main class
```text
@EnableAtpNotification
```

```properties
## How to send notifications
## kafka, rest or none; rest is default value
## If 'none' is selected, notifications are not sent.
atp.notification.mode=${ATP_NOTIFICATION_MODE:rest}

# Topic name to send notifications
kafka.notification.topic.name=${KAFKA_NOTIFICATION_TOPIC:notification_topic}
kafka.notification.topic.replicas=${KAFKA_NOTIFICATION_TOPIC_REPLICATION_FACTOR:3}
kafka.notification.topic.min.insync.replicas=${KAFKA_NOTIFICATION_TOPIC_MIN_INSYNC_REPLICATION_FACTOR:3}
kafka.notification.topic.partitions=${KAFKA_NOTIFICATION_TOPIC_PARTITIONS:1}
spring.kafka.producer.bootstrap-servers=${KAFKA_SERVERS:kafka:9092}
## feign client for atp-notification service
feign.atp.notification.url=${FEIGN_ATP_NOTIFICATION_URL:}
feign.atp.notification.name=${FEIGN_ATP_NOTIFICATION_NAME:atp-notification}
feign.atp.notification.route=${FEIGN_ATP_NOTIFICATION_ROUTE:/api/atp-notification/v1}
```

## Configuration for using the mail sender
```properties
## Enable sending mails via Kafka
kafka.mails.enable=${KAFKA_ENABLE:false}
## Settings for mail-sender topic
kafka.mails.topic=${KAFKA_MAILS_TOPIC:mails}
kafka.mails.topic.partitions=${KAFKA_MAILS_TOPIC_PARTITIONS:1}
kafka.mails.topic.replicas=${KAFKA_MAILS_TOPIC_REPLICAS:3}
kafka.mails.response.topic=${KAFKA_MAILS_RESPONSE_TOPIC_NAME:mail_responses}
## Maximum message size that can be accepted by the producer
kafka.mails.message.size=${KAFKA_MAILS_MESSAGE_SIZE:15728640}
## Message compression type
kafka.mails.compression.type=${KAFKA_MAILS_COMPRESSION_TYPE:lz4}
spring.kafka.producer.bootstrap-servers=${KAFKA_SERVERS:kafka:9092}
## Setting for mail-sender feign client
## Feign client is used if the email is sent with attachments or
## if kafka message size larger than maximal
feign.atp.mailsender.url=${FEIGN_ATP_MAILSENDER_URL:}
feign.atp.mailsender.name=${FEIGN_ATP_MAILSENDER_NAME:ATP-MAIL-SENDER}
feign.atp.mailsender.route=${FEIGN_ATP_MAILSENDER_ROUTE:}
```

Use the `send(MailRequest mailRequest)` or `send(MailRequest mailRequest, List<MultipartFile attachments)` methods in the `MailSenderService` class to send mail.
These methods return a `MailResponse` object.
If a mail request is sent to kafka, the mail response will only contain the message and the timestamp.

To get the results of sending an email through Kafka, you need to add a KafkaListener

```java
@KafkaListener(topic = "${kafka.mails.responses.topic}", groupId = "${kafka.mails.responses.group.id}")
public void consume(KafkaMailResponse mailResponse) {
    // This topic gets the results of sending emails from all services, 
    // so you need to add filtering by service name to separate your requests from other services
    if (yourServiceName.equals(mailResponse.getService())) {
        // Do whatever you want to do with response
        // For example just log response
        if (KafkaMailResponseStatus.SUCCESS.equals(mailResponse.getStatus())) {
            log.info("Mail sent successfully");
        } else {
            log.error("Sending an email failed with an error: {}", response.getMessage());
        }
    }    
}
```

## Audit Logging
### Add audit logging properties into application.properties
#### A. Kafka sender

Mandatory properties:
```properties
spring.kafka.producer.bootstrap-servers=...
atp.audit.logging.enable=true/false
atp.audit.logging.topic.name=...
```

Optional properties:
```properties
atp.audit.logging.topic.partitions=1
atp.audit.logging.topic.replicas=3
```
#### B. REST Feign sender

Mandatory properties:
```properties
atp.audit.logging.rest.enable=true/false
feign.atp.audit.logging.rest.url=http://your-audit-service:8080
```

Optional properties (don't set if url is set):
```properties
feign.atp.audit.logging.rest.name=atp-audit-service
feign.atp.audit.logging.rest.route=/api/v1/audit
```

## Logging business IDs
### Default list of business IDs
```properties
userId,projectId,executionRequestId,testRunId,bvTestRunId,bvTestCaseId,environmentId,
systemId,subscriberId,tsgSessionId,svpSessionId,dataSetId,dataSetListId,attributeId,
itfLiteRequestId,reportType,itfSessionId,itfContextId,callChainId
```

### Property to set business IDs
```properties
atp.logging.business.keys=userId,projectId
```
