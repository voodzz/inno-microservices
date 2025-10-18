package com.innowise.orderservice.controller;

import com.innowise.orderservice.model.StatusEnum;
import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.dto.OrderUserDto;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.service.impl.OrderService;
import com.innowise.orderservice.util.OrderSpecifications;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<OrderUserDto> create(@Valid @RequestBody OrderDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(dto));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderUserDto> findById(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.findById(id));
  }

  @GetMapping
  public ResponseEntity<Page<OrderUserDto>> find(
      @RequestParam(required = false) Collection<Long> ids,
      @RequestParam(required = false) Collection<StatusEnum> statuses,
      @PageableDefault Pageable pageable) {
    Specification<Order> specification =
        Specification.anyOf(
            OrderSpecifications.hasIdIn(ids), OrderSpecifications.hasStatusIn(statuses));
    return ResponseEntity.ok(orderService.findBySpecification(specification, pageable));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody OrderDto dto) {
    orderService.updateById(id, dto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    orderService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
