package com.online.lms.controller.admin;

import com.online.lms.entity.Setting;
import com.online.lms.service.SettingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/settings")
public class SettingController {

    @Autowired
    private SettingService settingService;

    @GetMapping
    public String listSettings(
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("priority").ascending());
        Page<Setting> settingPage = settingService.findAll(typeId, status, keyword, pageable);

        model.addAttribute("settings", settingPage);
        model.addAttribute("types", settingService.getAllTypes());
        return "admin/setting/setting-list";
    }

    @GetMapping("/status/{id}")
    public String toggleStatus(@PathVariable Long id) {
        settingService.toggleStatus(id);
        return "redirect:/admin/settings";
    }
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("setting", new Setting());
        model.addAttribute("types", settingService.getActiveMasterTypes());
        return "admin/setting/setting-detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Setting setting = settingService.findById(id);
        model.addAttribute("setting", setting);
        model.addAttribute("types", settingService.getActiveMasterTypes());
        return "admin/setting/setting-detail";
    }

    @PostMapping("/save")
    public String saveSetting(@Valid @ModelAttribute("setting") Setting setting,
                              BindingResult result, Model model) {

        // 1. Kiểm tra validation cơ bản (Annotation trong Entity/DTO)
        if (result.hasErrors()) {
            model.addAttribute("types", settingService.getActiveMasterTypes());
            return "admin/setting/setting-detail";
        }


        if (settingService.isNameDuplicate(setting.getId(), setting.getName(), setting.getType())) {
            result.rejectValue("name", "error.setting", "Name already exists in this type");
            model.addAttribute("types", settingService.getActiveMasterTypes());
            return "admin/setting/setting-detail";
        }

        settingService.save(setting);
        return "redirect:/admin/settings";
    }
}
