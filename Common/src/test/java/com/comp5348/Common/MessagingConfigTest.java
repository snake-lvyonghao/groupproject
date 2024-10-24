package com.comp5348.Common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MessagingConfigTest {

    @Autowired
    private Queue deliveryQueue;

    @Autowired
    private Queue emailQueue;

    @Test
    public void testQueuesAreCreated() {
        assertThat(deliveryQueue).isNotNull();
        assertThat(emailQueue).isNotNull();
    }
}
