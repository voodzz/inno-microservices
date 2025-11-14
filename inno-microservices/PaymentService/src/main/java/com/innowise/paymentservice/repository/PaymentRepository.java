package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.StatusEnum;
import com.innowise.paymentservice.model.dto.TotalSum;
import com.innowise.paymentservice.model.entity.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Spring Data repository for managing Payment documents in the 'payments' collection. Provides
 * standard CRUD operations via MongoRepository and custom query methods.
 */
public interface PaymentRepository extends MongoRepository<Payment, String> {

  /**
   * Inserts a new Payment entity into the MongoDB collection.
   *
   * @param entity The Payment entity to save.
   * @return The saved Payment entity with the generated ID.
   */
  Payment insert(Payment entity);

  /**
   * Retrieves a paginated list of Payments associated with a specific order ID.
   *
   * @param orderId The ID of the order.
   * @param pageable Pagination information (page number, size, and sort).
   * @return A list of matching Payment entities.
   */
  List<Payment> getPaymentsByOrderId(Long orderId, Pageable pageable);

  /**
   * Retrieves a paginated list of Payments associated with a specific user ID.
   *
   * @param userId The ID of the user.
   * @param pageable Pagination information (page number, size, and sort).
   * @return A list of matching Payment entities.
   */
  List<Payment> getPaymentsByUserId(Long userId, Pageable pageable);

  /**
   * Retrieves a paginated list of Payments that match any of the provided statuses.
   *
   * @param statuses A collection of {@link StatusEnum} values to filter by.
   * @param pageable Pagination information (page number, size, and sort).
   * @return A list of matching Payment entities.
   */
  List<Payment> getPaymentsByStatusIn(Collection<StatusEnum> statuses, Pageable pageable);

  /**
   * Calculates the total sum of all payment amounts within the specified date range. This uses a
   * MongoDB aggregation pipeline for efficient summation.
   *
   * @param start The start boundary of the time period (inclusive).
   * @param end The end boundary of the time period (inclusive).
   * @return The total sum of payments as a {@link BigDecimal}.
   */
  @Aggregation(
      pipeline = {
        "{ '$match' : { 'timestamp' : { '$gte' :  ?0, '$lte' :  ?1}}}",
        "{ '$group' : { '_id' : null, 'total' : { '$sum' : '$payment_amount' } } }"
      })
  TotalSum getTotalSumForPeriod(Instant start, Instant end);
}
