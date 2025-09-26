package com.innowise.userservice.service;

import java.util.Collection;
import java.util.List;

public interface CrudService<T, ID> {
  T create(T dto);

  T findById(ID id);

  List<T> findByIds(Collection<ID> ids);

  List<T> findAll();

  void update(ID id, T dto);

  void delete(ID id);
}
