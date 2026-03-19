package com.online.lms.service;

import com.online.lms.entity.Setting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SettingService {

    Page<Setting> findAll(Long typeId, String status, String keyword, Pageable pageable);


    List<Setting> getAllTypes();


    List<Setting> getActiveMasterTypes();


    boolean isNameDuplicate(Long id, String name, Setting type);


    void toggleStatus(Long id);


    Setting save(Setting setting);


    Setting findById(Long id);
}