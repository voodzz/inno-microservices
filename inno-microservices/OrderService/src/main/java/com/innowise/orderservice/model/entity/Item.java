package com.innowise.orderservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an item entity in the product catalog.
 *
 * <p>An item is a product that can be purchased by users and added to orders. It contains basic
 * information such as name and price, and maintains references to all order items where it has been
 * included.
 *
 * <p>This entity has a one-to-many relationship with {@link OrderItem} entities.
 *
 * @see OrderItem
 * @see Order
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "items")
public class Item {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(precision = 10, scale = 2, nullable = false)
  private BigDecimal price;

  @OneToMany(mappedBy = "item")
  private List<OrderItem> orderItems = new ArrayList<>();
}
