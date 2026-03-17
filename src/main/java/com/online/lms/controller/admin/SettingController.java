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

    // 1. Danh sách (Fix giữ tham số Search và Phân trang)
    @GetMapping
    public String listSettings(
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("priority").ascending());
        Page<Setting> settingPage = settingService.findAll(typeId, status, keyword, pageable);

        model.addAttribute("settingPage", settingPage); // Đổi tên thành settingPage cho chuẩn phân trang
        model.addAttribute("types", settingService.getAllTypes());

        // Trả lại dữ liệu search để hiển thị trên UI
        model.addAttribute("selectedTypeId", typeId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        return "admin/setting/setting-list";
    }

    // 2. Thay đổi trạng thái
    @GetMapping("/status/{id}")
    public String toggleStatus(@PathVariable Long id) {
        settingService.toggleStatus(id);
        return "redirect:/admin/settings";
    }

    // 3. Màn hình thêm mới (Fix bỏ code thừa)
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("setting", new Setting());
        model.addAttribute("types", settingService.getActiveMasterTypes());
        return "admin/setting/setting-detail";
    }

    // 4. Màn hình chỉnh sửa (Fix bỏ code thừa)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("setting", settingService.findById(id));
        model.addAttribute("types", settingService.getActiveMasterTypes());
        return "admin/setting/setting-detail";
    }

    // 5. Lưu dữ liệu (Fix xử lý lỗi và Type)
    @PostMapping("/save")
    public String saveSetting(@Valid @ModelAttribute("setting") Setting setting,
                              BindingResult result, Model model) {

        // Nếu người dùng chọn "-- None --" thì gán Type = null để tránh lỗi Foreign Key
        if (setting.getType() != null && setting.getType().getId() == null) {
            setting.setType(null);
        }

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