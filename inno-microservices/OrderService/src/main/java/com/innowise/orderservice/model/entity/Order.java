package com.innowise.orderservice.model.entity;

import com.innowise.orderservice.model.StatusEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an order entity in the system.
 *
 * <p>An order is a collection of items purchased by a user at a specific point in time. It tracks
 * the order status, creation date, and associated order items.
 *
 * <p>This entity maintains a one-to-many relationship with {@link OrderItem} entities, with cascade
 * operations and orphan removal enabled for proper lifecycle management.
 *
 * @see OrderItem
 * @see StatusEnum
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false, length = 64)
  @Enumerated(EnumType.STRING)
  private StatusEnum status;

  @Column(nullable = false)
  private LocalDate creationDate;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();
}
