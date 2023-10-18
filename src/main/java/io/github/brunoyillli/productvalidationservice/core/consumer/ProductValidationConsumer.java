package io.github.brunoyillli.productvalidationservice.core.consumer;



import io.github.brunoyillli.productvalidationservice.core.dto.Event;
import io.github.brunoyillli.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class ProductValidationConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-success}"
    )
    public void consumerSuccessEvent(String payload){
        log.info("Receiving success event {} from product-validation-success topic", payload);
        Event event = jsonUtil.toEvent(payload);
        log.info(event.toString());
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-fail}"
    )
    public void consumerFailEvent(String payload){
        log.info("Receiving rollback event {} from product-validation-fail topic", payload);
        Event event = jsonUtil.toEvent(payload);
        log.info(event.toString());
    }

}
