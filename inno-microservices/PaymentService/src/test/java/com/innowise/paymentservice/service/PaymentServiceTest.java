package com.innowise.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.messaging.event.OrderCreatedEvent;
import com.innowise.paymentservice.model.StatusEnum;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  private static final Long ORDER_ID = 42L;
  private static final Long USER_ID = 7L;
  private static final BigDecimal AMOUNT = BigDecimal.valueOf(199.99);

  @Mock private PaymentRepository paymentRepository;
  @Mock private RandomNumberService randomNumberService;

  @Mock private PaymentMapper paymentMapper;
  private PaymentService paymentService;

  @BeforeEach
  void setUp() {
    paymentService = new PaymentService(paymentRepository, paymentMapper, randomNumberService);
  }

  @Test
  void createPayment_shouldPersistSuccessfulPaymentWhenRandomIsEven() {
    when(randomNumberService.fetchRandomNumber()).thenReturn(2);
    when(paymentMapper.toEntity(any(PaymentDto.class)))
        .thenAnswer(
            invocation -> {
              PaymentDto dto = invocation.getArgument(0);
              Payment payment = new Payment();
              payment.setOrderId(dto.orderId());
              payment.setUserId(dto.userId());
              payment.setStatus(dto.status());
              payment.setTimestamp(dto.timestamp());
              payment.setPaymentAmount(dto.paymentAmount());
              return payment;
            });
    when(paymentRepository.insert(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              payment.setId("generated-id");
              return payment;
            });
    when(paymentMapper.toDto(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              return new PaymentDto(
                  payment.getId(),
                  payment.getOrderId(),
                  payment.getUserId(),
                  payment.getStatus(),
                  payment.getTimestamp(),
                  payment.getPaymentAmount());
            });

    PaymentDto result = paymentService.createPayment(new OrderCreatedEvent(ORDER_ID, USER_ID, AMOUNT, Instant.now()));

    assertThat(result.status()).isEqualTo(StatusEnum.SUCCESS);
    assertThat(result.paymentAmount()).isEqualByComparingTo(AMOUNT);
    assertThat(result.orderId()).isEqualTo(ORDER_ID);
    assertThat(result.userId()).isEqualTo(USER_ID);
  }

  @Test
  void createPaymentFromEvent_shouldPersistFailedPaymentWhenRandomIsOdd() {
    when(randomNumberService.fetchRandomNumber()).thenReturn(3);
    when(paymentMapper.toEntity(any(PaymentDto.class)))
        .thenAnswer(
            invocation -> {
              PaymentDto dto = invocation.getArgument(0);
              Payment payment = new Payment();
              payment.setOrderId(dto.orderId());
              payment.setUserId(dto.userId());
              payment.setStatus(dto.status());
              payment.setTimestamp(dto.timestamp());
              payment.setPaymentAmount(dto.paymentAmount());
              return payment;
            });

    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    when(paymentRepository.insert(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              payment.setId("generated-id-2");
              return payment;
            });
    when(paymentMapper.toDto(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              return new PaymentDto(
                  payment.getId(),
                  payment.getOrderId(),
                  payment.getUserId(),
                  payment.getStatus(),
                  payment.getTimestamp(),
                  payment.getPaymentAmount());
            });

    OrderCreatedEvent event = new OrderCreatedEvent(ORDER_ID, USER_ID, AMOUNT, Instant.now());

    PaymentDto result = paymentService.createPayment(event);

    assertThat(result.status()).isEqualTo(StatusEnum.FAILED);
    assertThat(result.paymentAmount()).isEqualByComparingTo(AMOUNT);

    org.mockito.Mockito.verify(paymentRepository).insert(captor.capture());
    assertThat(captor.getValue().getStatus()).isEqualTo(StatusEnum.FAILED);
  }

  @Test
  void createPayment_shouldPersistSuccessfulPaymentWhenRandomIsZero() {
    when(randomNumberService.fetchRandomNumber()).thenReturn(0);
    when(paymentMapper.toEntity(any(PaymentDto.class)))
        .thenAnswer(
            invocation -> {
              PaymentDto dto = invocation.getArgument(0);
              Payment payment = new Payment();
              payment.setOrderId(dto.orderId());
              payment.setUserId(dto.userId());
              payment.setStatus(dto.status());
              payment.setTimestamp(dto.timestamp());
              payment.setPaymentAmount(dto.paymentAmount());
              return payment;
            });
    when(paymentRepository.insert(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              payment.setId("generated-id-3");
              return payment;
            });
    when(paymentMapper.toDto(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              return new PaymentDto(
                  payment.getId(),
                  payment.getOrderId(),
                  payment.getUserId(),
                  payment.getStatus(),
                  payment.getTimestamp(),
                  payment.getPaymentAmount());
            });

    OrderCreatedEvent event = new OrderCreatedEvent(ORDER_ID, USER_ID, AMOUNT, Instant.now());
    PaymentDto result = paymentService.createPayment(event);

    assertThat(result.status()).isEqualTo(StatusEnum.SUCCESS);
    assertThat(result.id()).isEqualTo("generated-id-3");
    assertThat(result.orderId()).isEqualTo(ORDER_ID);
    assertThat(result.userId()).isEqualTo(USER_ID);
    assertThat(result.paymentAmount()).isEqualByComparingTo(AMOUNT);
  }

  @Test
  void createPayment_shouldPersistSuccessfulPaymentWhenRandomIsNegativeEven() {
    when(randomNumberService.fetchRandomNumber()).thenReturn(-4);
    when(paymentMapper.toEntity(any(PaymentDto.class)))
        .thenAnswer(
            invocation -> {
              PaymentDto dto = invocation.getArgument(0);
              Payment payment = new Payment();
              payment.setOrderId(dto.orderId());
              payment.setUserId(dto.userId());
              payment.setStatus(dto.status());
              payment.setTimestamp(dto.timestamp());
              payment.setPaymentAmount(dto.paymentAmount());
              return payment;
            });
    when(paymentRepository.insert(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              payment.setId("generated-id-4");
              return payment;
            });
    when(paymentMapper.toDto(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              return new PaymentDto(
                  payment.getId(),
                  payment.getOrderId(),
                  payment.getUserId(),
                  payment.getStatus(),
                  payment.getTimestamp(),
                  payment.getPaymentAmount());
            });

    OrderCreatedEvent event = new OrderCreatedEvent(ORDER_ID, USER_ID, AMOUNT, Instant.now());
    PaymentDto result = paymentService.createPayment(event);

    assertThat(result.status()).isEqualTo(StatusEnum.SUCCESS);
  }

  @Test
  void createPayment_shouldPersistFailedPaymentWhenRandomIsNegativeOdd() {
    when(randomNumberService.fetchRandomNumber()).thenReturn(-5);
    when(paymentMapper.toEntity(any(PaymentDto.class)))
        .thenAnswer(
            invocation -> {
              PaymentDto dto = invocation.getArgument(0);
              Payment payment = new Payment();
              payment.setOrderId(dto.orderId());
              payment.setUserId(dto.userId());
              payment.setStatus(dto.status());
              payment.setTimestamp(dto.timestamp());
              payment.setPaymentAmount(dto.paymentAmount());
              return payment;
            });
    when(paymentRepository.insert(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              payment.setId("generated-id-5");
              return payment;
            });
    when(paymentMapper.toDto(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              return new PaymentDto(
                  payment.getId(),
                  payment.getOrderId(),
                  payment.getUserId(),
                  payment.getStatus(),
                  payment.getTimestamp(),
                  payment.getPaymentAmount());
            });

    OrderCreatedEvent event = new OrderCreatedEvent(ORDER_ID, USER_ID, AMOUNT, Instant.now());
    PaymentDto result = paymentService.createPayment(event);

    assertThat(result.status()).isEqualTo(StatusEnum.FAILED);
  }

  @Test
  void createPayment_shouldSetCorrectTimestamp() {
    Instant testTimestamp = Instant.parse("2024-01-01T12:00:00Z");
    when(randomNumberService.fetchRandomNumber()).thenReturn(2);
    when(paymentMapper.toEntity(any(PaymentDto.class)))
        .thenAnswer(
            invocation -> {
              PaymentDto dto = invocation.getArgument(0);
              Payment payment = new Payment();
              payment.setOrderId(dto.orderId());
              payment.setUserId(dto.userId());
              payment.setStatus(dto.status());
              payment.setTimestamp(dto.timestamp());
              payment.setPaymentAmount(dto.paymentAmount());
              return payment;
            });
    
    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    when(paymentRepository.insert(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              payment.setId("generated-id-6");
              return payment;
            });
    when(paymentMapper.toDto(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              return new PaymentDto(
                  payment.getId(),
                  payment.getOrderId(),
                  payment.getUserId(),
                  payment.getStatus(),
                  payment.getTimestamp(),
                  payment.getPaymentAmount());
            });

    OrderCreatedEvent event = new OrderCreatedEvent(ORDER_ID, USER_ID, AMOUNT, testTimestamp);
    PaymentDto result = paymentService.createPayment(event);

    org.mockito.Mockito.verify(paymentRepository).insert(captor.capture());
    Payment savedPayment = captor.getValue();
    assertThat(savedPayment.getTimestamp()).isNotNull();
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void createPayment_shouldMapEntityToDtoCorrectly() {
    when(randomNumberService.fetchRandomNumber()).thenReturn(4);
    when(paymentMapper.toEntity(any(PaymentDto.class)))
        .thenAnswer(
            invocation -> {
              PaymentDto dto = invocation.getArgument(0);
              Payment payment = new Payment();
              payment.setOrderId(dto.orderId());
              payment.setUserId(dto.userId());
              payment.setStatus(dto.status());
              payment.setTimestamp(dto.timestamp());
              payment.setPaymentAmount(dto.paymentAmount());
              return payment;
            });
    
    Payment savedPayment = new Payment("saved-id", ORDER_ID, USER_ID, StatusEnum.SUCCESS, Instant.now(), AMOUNT);
    PaymentDto expectedDto = new PaymentDto("saved-id", ORDER_ID, USER_ID, StatusEnum.SUCCESS, savedPayment.getTimestamp(), AMOUNT);
    
    when(paymentRepository.insert(any(Payment.class))).thenReturn(savedPayment);
    when(paymentMapper.toDto(savedPayment)).thenReturn(expectedDto);

    OrderCreatedEvent event = new OrderCreatedEvent(ORDER_ID, USER_ID, AMOUNT, Instant.now());
    PaymentDto result = paymentService.createPayment(event);

    assertThat(result).isEqualTo(expectedDto);
    org.mockito.Mockito.verify(paymentMapper).toDto(savedPayment);
  }
}


