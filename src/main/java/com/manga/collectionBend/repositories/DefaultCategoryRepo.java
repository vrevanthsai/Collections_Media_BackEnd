package com.manga.collectionBend.repositories;

import com.manga.collectionBend.entities.DefaultCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultCategoryRepo extends JpaRepository<DefaultCategoryEntity, Integer> {
    List<DefaultCategoryEntity> findByActiveTrue();

    boolean existsByCategoryNameAndIdNot(String categoryName, Integer id);
}
