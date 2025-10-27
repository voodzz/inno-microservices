package com.innowise.orderservice.util;

import com.innowise.orderservice.model.StatusEnum;
import com.innowise.orderservice.model.entity.Order;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

@UtilityClass
public class OrderSpecifications {

    public Specification<Order> hasIdIn(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return (root,query,criteriaBuilder) -> root.get("id").in(ids);
    }

    public Specification<Order> hasStatusIn(Collection<StatusEnum> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> root.get("status").in(statuses);
    }

    public Specification<Order> all() {
        return Specification.unrestricted();
    }
}
