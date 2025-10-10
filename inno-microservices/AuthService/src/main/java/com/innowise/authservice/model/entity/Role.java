package com.innowise.authservice.model.entity;

import com.innowise.authservice.model.RoleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;

/**
 * Entity representing a user's role in the system.
 *
 * <p>This entity is mapped to the {@code roles} table and defines the association between a {@link User}
 * and a specific {@link RoleEnum} value, such as {@code ROLE_ADMIN} or {@code ROLE_USER}.
 *
 * <p>Implements {@link org.springframework.security.core.GrantedAuthority} to integrate with Spring
 * Security's authorization mechanisms.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority {
  @Serial private static final long serialVersionUID = -3242589910858464110L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 128)
  @Enumerated(value = EnumType.STRING)
  private RoleEnum role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private User user;

  @Override
  public String getAuthority() {
    return role.toString();
  }
}
