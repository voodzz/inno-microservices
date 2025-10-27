package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.StatusEnum;
import com.innowise.orderservice.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository interface for managing {@link Order} entities.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD operations and adds custom query
 * methods for user-specific lookups and updates.
 */
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

  /**
   * Saves the given order entity.
   *
   * @param entity the order entity to save
   * @return the persisted order entity
   */
  @Override
  Order save(Order entity);

  /**
   * Retrieves an order by its unique identifier.
   *
   * @param id the ID of the Order
   * @return an {@link Optional} containing the order if found, otherwise empty
   */
  @Query("SELECT o FROM Order o WHERE o.id = :id")
  Optional<Order> findOrderById(@Param("id") Long id);

  /**
   * Retrieves all orders
   *
   * @param pageable the pageable to request a paged result, can be {@link Pageable#unpaged()}, must
   *     not be {@literal null}.
   * @return a page of all orders
   */
  @Override
  Page<Order> findAll(Pageable pageable);

  /**
   * Updates the status of an order by the given ID.
   *
   * @param id the ID of the order to update
   * @param status the new status of the order
   * @return the number of affected rows (should be {@code 1} if the order was updated)
   */
  @Transactional
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
  int updateById(@Param("id") Long id, @Param("status") StatusEnum status);

  /**
   * Deletes the order by its ID
   *
   * @param id the ID of the order to delete
   */
  @Override
  void deleteById(Long id);
}
