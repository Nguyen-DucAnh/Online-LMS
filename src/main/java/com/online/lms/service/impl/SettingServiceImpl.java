package com.online.lms.service.impl;

import com.online.lms.entity.Setting;
import com.online.lms.repository.SettingRepository;
import com.online.lms.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SettingServiceImpl implements SettingService {

    @Autowired
    private SettingRepository settingRepository;

    @Override
    public Page<Setting> findAll(Long typeId, String status, String keyword, Pageable pageable) {

        return settingRepository.findAllCustom(typeId, status, keyword, pageable);
    }

    @Override
    public List<Setting> getAllTypes() {

        return settingRepository.findAll().stream()
                .filter(s -> s.getType() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Setting> getActiveMasterTypes() {

        return settingRepository.findAll().stream()
                .filter(s -> s.getType() == null && "Active".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isNameDuplicate(Long id, String name, Setting type) {

        return settingRepository.findAll().stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(name)
                        && ( (s.getType() == null && type == null) || (s.getType() != null && s.getType().equals(type)) )
                        && (id == null || !s.getId().equals(id)));
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Setting setting = findById(id);
        if ("Active".equalsIgnoreCase(setting.getStatus())) {
            setting.setStatus("Inactive");
        } else {
            setting.setStatus("Active");
        }
        settingRepository.save(setting);
    }

    @Override
    @Transactional
    public Setting save(Setting setting) {

        return settingRepository.save(setting);
    }

    @Override
    public Setting findById(Long id) {
        return settingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Setting not found with id: " + id));
    }
}