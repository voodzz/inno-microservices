package com.innowise.orderservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Generic CRUD (Create, Read, Update, Delete) service interface for managing entities.
 *
 * @param <IN> the type of the Data Transfer Object (DTO) representing for input
 * @param <OUT> the type of the Data Transfer Object (DTO) representing for output
 * @param <ID> the type of the unique identifier of the entity
 */
public interface CrudService<IN, OUT, ID> {

  /**
   * Creates a new entity.
   *
   * @param dto the DTO representing the entity to create
   * @return the created entity DTO
   */
  OUT create(IN dto);

  /**
   * Finds an entity by its unique identifier.
   *
   * @param id the identifier of the entity
   * @return the entity DTO if found, or {@code null} if not found
   */
  OUT findById(ID id);

  /**
   * Retrieves all entities
   *
   * @param pageable the pagination information
   * @return a page of all the entities
   */
  Page<OUT> findAll(Pageable pageable);

  /**
   * Updates the existing entity
   *
   * @param id the identifier of the entity to update
   * @param dto the DTO containing updated entity data
   */
  OUT updateById(ID id, IN dto);

  /**
   * Deletes an entity by its identifier.
   *
   * @param id the identifier of the entity to delete
   */
  void deleteById(ID id);
}
