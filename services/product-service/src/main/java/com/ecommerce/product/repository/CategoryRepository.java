package com.ecommerce.product.repository;

import com.ecommerce.product.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity.
 */
@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIdIsNullAndActiveTrue();

    List<Category> findByParentIdAndActiveTrue(String parentId);

    List<Category> findByActiveTrueOrderByDisplayOrder();
}
