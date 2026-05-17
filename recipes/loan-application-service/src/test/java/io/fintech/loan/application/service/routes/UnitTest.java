package io.fintech.loan.application.service.routes;

import io.fintech.loan.application.service.CamelbeeServiceApplication;
import io.fintech.loan.application.service.UnitTestConfig;
import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@CamelSpringBootTest
@SpringBootTest(classes = {CamelbeeServiceApplication.class, UnitTestConfig.class},
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "spring.main.allow-bean-definition-overriding=true",
        "camelbee.context-enabled=false",
        "camelbee.tracer-enabled=false"
    })
@UseAdviceWith
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class UnitTest {

  @Autowired
  protected FluentProducerTemplate fluentProducerTemplate;

  @Autowired
  protected CamelContext camelContext;
}
