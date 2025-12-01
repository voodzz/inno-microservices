package com.innowise.orderservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderUserDto {
  private OrderDto orderDto;
  private UserDto userDto;
}
