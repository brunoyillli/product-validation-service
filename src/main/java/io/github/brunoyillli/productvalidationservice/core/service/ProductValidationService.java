package io.github.brunoyillli.productvalidationservice.core.service;

import io.github.brunoyillli.productvalidationservice.config.exception.ValidationException;
import io.github.brunoyillli.productvalidationservice.core.dto.Event;
import io.github.brunoyillli.productvalidationservice.core.dto.History;
import io.github.brunoyillli.productvalidationservice.core.dto.OrderProducts;
import io.github.brunoyillli.productvalidationservice.core.enums.ESagaStatus;
import io.github.brunoyillli.productvalidationservice.core.model.Validation;
import io.github.brunoyillli.productvalidationservice.core.producer.KafkaProducer;
import io.github.brunoyillli.productvalidationservice.core.repository.ProductRepository;
import io.github.brunoyillli.productvalidationservice.core.repository.ValidationRepository;
import io.github.brunoyillli.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class ProductValidationService {

    private static final String CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";

    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;
    private final ProductRepository productRepository;
    private final ValidationRepository validationRepository;

    public void validateExistingProducts(Event event){
        try {
            checkCurrentValidation(event);
            createValidation(event, true);
            handleSuccess(event);
        }catch (Exception ex){
            log.error("Error trying to validate products: ", ex);
            handleFailCurrentNotExecuted(event, ex.getMessage());
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }

    private void checkCurrentValidation(Event event){
        validateProductsInformed(event);
        if(validationRepository.existsByOrderIdAndTransactionId(
                event.getOrderId(), event.getTransactionId()
        )){
            throw new ValidationException("There's another transactionId for this validation.");
        }
        event.getPayload().getProducts().forEach(product -> {
            validateProductInformed(product);
            validateExistingProduct(product.getProduct().getCode());
        });
    }

    private void validateProductsInformed(Event event){
        if(isEmpty(event.getPayload()) || isEmpty(event.getPayload().getProducts())){
            throw new ValidationException("Product list is empty!");
        }

        if(isEmpty(event.getPayload().getId()) || isEmpty(event.getPayload().getTransactionId())){
            throw new ValidationException("OrderID and TransactionID must be informed");
        }
    }

    private void validateProductInformed(OrderProducts products){
        if(isEmpty(products.getProduct()) || isEmpty(products.getProduct().getCode())){
            throw new ValidationException("Product must be informed");
        }
    }

    public void validateExistingProduct(String code){
        if(!productRepository.existsByCode(code)){
            throw new ValidationException("Product does not exists in database!");
        }
    }

    private void createValidation(Event event, boolean success){
        Validation validation = Validation.builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .success(success)
                .build();
        validationRepository.save(validation);
    }

    private void handleSuccess(Event event){
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Products are validated successfully!");
    }

    private void addHistory(Event event,String message){
        History history = History.builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }

}
