package com.innowise.orderservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

/**
 * Generic CRUD (Create, Read, Update, Delete) service interface for managing entities.
 *
 * @param <T> the type of the Data Transfer Object (DTO) representing the entity
 * @param <ID> the type of the unique identifier of the entity
 */
public interface CrudService<T, ID> {

  /**
   * Creates a new entity.
   *
   * @param dto the DTO representing the entity to create
   * @return the created entity DTO
   */
  T create(T dto);

  /**
   * Finds an entity by its unique identifier.
   *
   * @param id the identifier of the entity
   * @return the entity DTO if found, or {@code null} if not found
   */
  T findById(ID id);

  /**
   * Finds multiple entities by their unique ID
   *
   * @param ids the collection of entity identifiers
   * @param pageable the pagination information
   * @return a page of orders matching the provided IDs
   */
  Page<T> findByIds(Collection<ID> ids, Pageable pageable);

  /**
   * Retrieves all entities
   *
   * @param pageable the pagination information
   * @return a page of all the entities
   */
  Page<T> findAll(Pageable pageable);

  /**
   * Updates the existing entity
   *
   * @param id the identifier of the entity to update
   * @param dto the DTO containing updated entity data
   */
  void updateById(ID id, T dto);

  /**
   * Deletes an entity by its identifier.
   *
   * @param id the identifier of the entity to delete
   */
  void deleteById(ID id);
}
