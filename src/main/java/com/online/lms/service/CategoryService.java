package com.online.lms.service;

import com.online.lms.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> findAllActive();

    List<Category> findAll();
    
    Category findById(Long id);
    
    void save(Category category);
    
    void delete(Long id);
}
