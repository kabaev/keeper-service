package com.kabaev.shop.service.keeper.repository;

import com.kabaev.shop.service.keeper.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
