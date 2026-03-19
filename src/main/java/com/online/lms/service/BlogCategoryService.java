package com.online.lms.service;

import com.online.lms.entity.BlogCategory;

import java.util.List;

public interface BlogCategoryService {

    List<BlogCategory> findAllActive();

    List<BlogCategory> findAll();
}
