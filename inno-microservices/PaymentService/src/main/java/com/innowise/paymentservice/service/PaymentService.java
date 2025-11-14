package com.innowise.paymentservice.service;

import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.StatusEnum;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.messaging.event.OrderCreatedEvent;
import com.innowise.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;
  private final RandomNumberService randomNumberService;

  @Transactional
  public PaymentDto createPayment(OrderCreatedEvent event) {
    StatusEnum status = mapNumberToStatus(randomNumberService.fetchRandomNumber());
    PaymentDto paymentDto =
        new PaymentDto(
            null, event.orderId(), event.userId(), status, Instant.now(), event.totalAmount());

    Payment saved = paymentRepository.insert(paymentMapper.toEntity(paymentDto));
    return paymentMapper.toDto(saved);
  }

  private StatusEnum mapNumberToStatus(int randomNumber) {
    return randomNumber % 2 == 0 ? StatusEnum.SUCCESS : StatusEnum.FAILED;
  }
}
