package com.online.lms.service;

import com.online.lms.entity.Setting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SettingService {
    // Tìm kiếm và phân trang cho trang danh sách
    Page<Setting> findAll(Long typeId, String status, String keyword, Pageable pageable);

    // Lấy tất cả các loại setting (để lọc ở trang list)
    List<Setting> getAllTypes();

    // Lấy các loại setting đang hoạt động (để chọn Type trong trang detail)
    List<Setting> getActiveMasterTypes();

    // Kiểm tra trùng tên trong cùng một nhóm Type
    boolean isNameDuplicate(Long id, String name, Setting type);

    // Đảo trạng thái Active/Inactive
    void toggleStatus(Long id);

    // Lưu thông tin setting
    Setting save(Setting setting);

    // Tìm kiếm theo ID
    Setting findById(Long id);
}