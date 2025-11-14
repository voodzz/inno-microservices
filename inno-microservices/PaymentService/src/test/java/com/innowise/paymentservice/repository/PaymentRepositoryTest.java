package com.innowise.paymentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.innowise.paymentservice.model.StatusEnum;
import com.innowise.paymentservice.model.dto.TotalSum;
import com.innowise.paymentservice.model.entity.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataMongoTest
@ActiveProfiles("test")
@Testcontainers
class PaymentRepositoryTest {

  @Container
  private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:8.0.3");

  @DynamicPropertySource
  static void registerMongoProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    registry.add("mongo.url", MONGO_DB_CONTAINER::getReplicaSetUrl);
  }

  @Autowired private PaymentRepository paymentRepository;

  private final PageRequest pageable = PageRequest.of(0, 10, Sort.by("timestamp").descending());

  private final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);
  private final Instant YESTERDAY = NOW.minus(1, ChronoUnit.DAYS);
  private final Instant WEEK_AGO = NOW.minus(7, ChronoUnit.DAYS);

  @BeforeEach
  void setUp() {
    paymentRepository.deleteAll();

    List<Payment> payments =
        Arrays.asList(
            new Payment("1", 1001L, 501L, StatusEnum.SUCCESS, YESTERDAY, BigDecimal.valueOf(99.99)),
            new Payment("2", 1002L, 502L, StatusEnum.SUCCESS, NOW, BigDecimal.valueOf(450.00)),
            new Payment("3", 1001L, 501L, StatusEnum.FAILED, YESTERDAY, BigDecimal.valueOf(12.50)),
            new Payment("4", 1003L, 503L, StatusEnum.FAILED, WEEK_AGO, BigDecimal.valueOf(200.00)),
            new Payment("5", 1004L, 504L, StatusEnum.SUCCESS, NOW, BigDecimal.valueOf(75.05)));

    paymentRepository.saveAll(payments);
  }

  @Test
  void getTotalSumForPeriod_shouldReturnCorrectSumForTimeRange() {
    Instant start = YESTERDAY.minus(1, ChronoUnit.MINUTES);
    Instant end = NOW.plus(1, ChronoUnit.MINUTES);

    BigDecimal expectedSum = new BigDecimal("637.54");

    TotalSum result = paymentRepository.getTotalSumForPeriod(start, end);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).isEqualByComparingTo(expectedSum);
  }

  @Test
  void getPaymentsByOrderId_shouldReturnPaymentsForSpecificOrder() {
    Long targetOrderId = 1001L;

    List<Payment> payments = paymentRepository.getPaymentsByOrderId(targetOrderId, pageable);

    assertThat(payments).hasSize(2);
    assertThat(payments).extracting(Payment::getOrderId).containsOnly(targetOrderId);
    assertThat(payments).extracting(Payment::getId).containsExactlyInAnyOrder("1", "3");
  }

  @Test
  void getPaymentsByStatusIn_shouldReturnPaymentsForGivenStatuses() {
    List<StatusEnum> statuses = Arrays.asList(StatusEnum.SUCCESS, StatusEnum.FAILED);

    List<Payment> payments = paymentRepository.getPaymentsByStatusIn(statuses, pageable);

    assertThat(payments).hasSize(5);
    assertThat(payments)
        .extracting(Payment::getStatus)
        .containsOnly(StatusEnum.SUCCESS, StatusEnum.FAILED);
  }
}
