package com.online.lms.service.impl;

import com.online.lms.entity.Category;
import com.online.lms.repository.CategoryRepository;
import com.online.lms.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Scope("singleton")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> findAllActive() {
        return categoryRepository.findByStatusTrue();
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}
