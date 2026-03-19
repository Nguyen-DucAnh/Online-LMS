package com.online.lms.service.impl;

import com.online.lms.entity.BlogCategory;
import com.online.lms.repository.BlogCategoryRepository;
import com.online.lms.service.BlogCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogCategoryServiceImpl implements BlogCategoryService {

    private final BlogCategoryRepository blogCategoryRepository;

    @Override
    public List<BlogCategory> findAllActive() {
        return blogCategoryRepository.findByStatusTrue();
    }

    @Override
    public List<BlogCategory> findAll() {
        return blogCategoryRepository.findAll();
    }
}
