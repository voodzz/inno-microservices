package com.innowise.paymentservice.service;

import com.innowise.paymentservice.exception.AlreadyExistsException;
import com.innowise.paymentservice.exception.ExternalApiException;
import com.innowise.paymentservice.exception.PaymentCreationException;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.StatusEnum;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.messaging.event.OrderCreatedEvent;
import com.innowise.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;
  private final RandomNumberService randomNumberService;

  public PaymentDto createPayment(OrderCreatedEvent event) {
    if (paymentRepository.existsByOrderId(event.orderId())) {
      throw new AlreadyExistsException(
          "Payment for the order with ID '%s' already exists".formatted(event.orderId()));
    }
    try {
      StatusEnum status = mapNumberToStatus(randomNumberService.fetchRandomNumber());
      PaymentDto paymentDto =
          new PaymentDto(
              null, event.orderId(), event.userId(), status, Instant.now(), event.totalAmount());

      Payment saved = paymentRepository.insert(paymentMapper.toEntity(paymentDto));
      return paymentMapper.toDto(saved);
    } catch (ExternalApiException ex) {
      throw new PaymentCreationException("Creation of payment failed", ex);
    }
  }

  private StatusEnum mapNumberToStatus(int randomNumber) {
    return randomNumber % 2 == 0 ? StatusEnum.SUCCESS : StatusEnum.FAILED;
  }
}
