package com.manga.collectionBend.repositories;

import com.manga.collectionBend.entities.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepo extends JpaRepository<CategoryEntity,Integer> {
//       Custom Method to get(GET) User Based Collections Data
//   naming formate- findBy+CapitalFirstLetter of Field/Column name(eg-Username)
//    findBy + User(parent-Category-table/entity field name) + UserId(is child-UserTable/Entity field name which is inside parent-Category table)
    List<CategoryEntity> findByUserUserId(Integer userId);
}
