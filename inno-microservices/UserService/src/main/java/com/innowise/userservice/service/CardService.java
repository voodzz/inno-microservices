package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.CardDto;

/**
 * Service interface for managing cards. Extends {@link CrudService} to provide standard CRUD
 * operations for {@link CardDto}.
 */
public interface CardService extends CrudService<CardDto, Long> {}
