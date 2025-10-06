package com.innowise.userservice.service;

import java.util.Collection;
import java.util.List;

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
   * Finds multiple entities by their identifiers.
   *
   * @param ids the collection of entity identifiers
   * @return a list of found entity DTOs; if none are found, returns an empty list
   */
  List<T> findByIds(Collection<ID> ids);

  /**
   * Retrieves all entities.
   *
   * @return a list of all entity DTOs; if none exist, returns an empty list
   */
  List<T> findAll();

  /**
   * Updates an existing entity.
   *
   * @param id the identifier of the entity to update
   * @param dto the DTO containing updated entity data
   */
  void update(ID id, T dto);

  /**
   * Deletes an entity by its identifier.
   *
   * @param id the identifier of the entity to delete
   */
  void delete(ID id);
}
