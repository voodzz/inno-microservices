package com.innowise.paymentservice.model.entity;

import com.innowise.paymentservice.model.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;

    @Field("order_id")
    private Long orderId;

    @Field("user_id")
    private Long userId;

    private StatusEnum status;

    private Instant timestamp;

    @Field("payment_amount")
    private BigDecimal paymentAmount;
}
