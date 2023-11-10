package io.github.brunoyillli.productvalidationservice.core.dto;


import io.github.brunoyillli.productvalidationservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.ObjectUtils;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    private String id;
    private String transactionId;
    private String orderId;
    private Order payload;
    private String source;
    private ESagaStatus status;
    private List<History> eventHistory;
    private LocalDateTime createdAt;

    public void addToHistory(History history){
        if(ObjectUtils.isEmpty(this.eventHistory)){
            this.eventHistory = new ArrayList<>();
        }
        this.eventHistory.add(history);
    }

}
