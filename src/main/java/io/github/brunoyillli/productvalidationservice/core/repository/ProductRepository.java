package io.github.brunoyillli.productvalidationservice.core.repository;

import io.github.brunoyillli.productvalidationservice.core.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Boolean existsByCode(String code);
}
