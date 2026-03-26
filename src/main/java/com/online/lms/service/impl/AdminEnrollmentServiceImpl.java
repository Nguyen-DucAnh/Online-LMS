package com.online.lms.service.impl;

import com.online.lms.dto.enrollment.EnrollmentFormDTO;
import com.online.lms.dto.enrollment.EnrollmentListItemDTO;
import com.online.lms.entity.Course;
import com.online.lms.entity.Enrollment;
import com.online.lms.entity.User;
import com.online.lms.enums.EnrollmentStatus;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.repository.CourseRepository;
import com.online.lms.repository.EnrollmentRepository;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.AdminEnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEnrollmentServiceImpl implements AdminEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository     courseRepository;
    private final UserRepository       userRepository;

    @Override
    public Page<EnrollmentListItemDTO> findAll(Long courseId, Long userId,
                                               EnrollmentStatus status, String keyword,
                                               Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return enrollmentRepository
                .findAllWithFilter(courseId, userId, status, kw, pageable)
                .map(this::toListItemDTO);
    }

    @Override
    public Page<EnrollmentListItemDTO> findByInstructor(Long instructorId, Long courseId,
                                                        EnrollmentStatus status, String keyword,
                                                        Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return enrollmentRepository
                .findByInstructorWithFilter(instructorId, courseId, status, kw, pageable)
                .map(this::toListItemDTO);
    }

    // ── Form ──────────────────────────────────────────────────────────────────

    @Override
    public EnrollmentFormDTO findFormById(Long id) {
        Enrollment e = getOrThrow(id);
        return toFormDTO(e);
    }

    @Override
    @Transactional
    public void save(EnrollmentFormDTO dto) {
        if (dto.getId() == null) {
            // Tạo mới
            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy khóa học id=" + dto.getCourseId()));

            User user = findUserByUsernameOrEmail(dto.getUsernameOrEmail());

            // Enrollment entity chỉ có @Getter @Setter (không có @Builder)
            Enrollment e = new Enrollment();
            e.setCourse(course);
            e.setUser(user);
            e.setFullName(dto.getFullName() != null ? dto.getFullName() : user.getFullName());
            e.setEmail(user.getEmail());
            e.setPhone(user.getPhone());
            e.setEnrollNote(dto.getEnrollNote());
            e.setStatus(dto.getStatus() != null ? dto.getStatus() : EnrollmentStatus.PENDING);
            e.setFee(dto.getFee() != null ? dto.getFee() : BigDecimal.ZERO);
            e.setProgress(BigDecimal.ZERO);
            if (dto.getStatus() == EnrollmentStatus.REJECTED) {
                e.setRejectedNotes(dto.getRejectedNotes());
            }
            enrollmentRepository.save(e);
            log.info("Enrollment created: courseId={}, userId={}", course.getId(), user.getId());
        } else {
            // Cập nhật
            Enrollment e = getOrThrow(dto.getId());
            e.setFullName(dto.getFullName());
            e.setEnrollNote(dto.getEnrollNote());
            e.setStatus(dto.getStatus());
            e.setFee(dto.getFee() != null ? dto.getFee() : BigDecimal.ZERO);

            if (dto.getStatus() == EnrollmentStatus.REJECTED) {
                if (dto.getRejectedNotes() == null || dto.getRejectedNotes().isBlank()) {
                    throw new IllegalArgumentException("Vui lòng nhập lý do từ chối.");
                }
                e.setRejectedNotes(dto.getRejectedNotes());
            } else {
                e.setRejectedNotes(null);
            }

            if (dto.getProgress() != null) e.setProgress(dto.getProgress());
            if (dto.getCompletedAt() != null) e.setCompletedAt(dto.getCompletedAt());

            enrollmentRepository.save(e);
            log.info("Enrollment updated: id={}", e.getId());
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void approve(Long id) {
        Enrollment e = getOrThrow(id);
        e.setStatus(EnrollmentStatus.APPROVED);
        e.setRejectedNotes(null);
        enrollmentRepository.save(e);
        log.info("Enrollment approved: id={}", id);
    }

    @Override
    @Transactional
    public void reject(Long id, String rejectedNotes) {
        if (rejectedNotes == null || rejectedNotes.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối.");
        }
        Enrollment e = getOrThrow(id);
        e.setStatus(EnrollmentStatus.REJECTED);
        e.setRejectedNotes(rejectedNotes);
        enrollmentRepository.save(e);
        log.info("Enrollment rejected: id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        enrollmentRepository.delete(getOrThrow(id));
        log.info("Enrollment deleted: id={}", id);
    }

    @Override
    public boolean hasManagerAccess(Long enrollmentId, Long managerId) {
        return enrollmentRepository.existsByIdAndInstructorId(enrollmentId, managerId);
    }

    // ── Export CSV ────────────────────────────────────────────────────────────

    @Override
    public byte[] exportToCsv(Long courseId, EnrollmentStatus status) throws IOException {
        // Lấy tất cả (không phân trang) để export
        var enrollments = enrollmentRepository.findAllForExport(courseId, status);
        return toCsvBytes(enrollments);
    }

    @Override
    public byte[] exportToCsvByInstructor(Long instructorId, Long courseId, EnrollmentStatus status) throws IOException {
        var enrollments = enrollmentRepository.findAllForExportByInstructor(instructorId, courseId, status);
        return toCsvBytes(enrollments);
    }

    private byte[] toCsvBytes(List<Enrollment> enrollments) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Course,Full Name,Email,Phone,Enrolled At,Fee,Status,Progress\n");
        for (Enrollment e : enrollments) {
            sb.append(e.getId()).append(",")
                    .append(csvEscape(e.getCourse().getTitle())).append(",")
                    .append(csvEscape(e.getFullName())).append(",")
                    .append(csvEscape(e.getEmail())).append(",")
                    .append(csvEscape(e.getPhone())).append(",")
                    .append(e.getEnrollDate()).append(",")
                    .append(e.getFee()).append(",")
                    .append(e.getStatus()).append(",")
                    .append(e.getProgress()).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ── Import Excel ──────────────────────────────────────────────────────────

    /**
     * Import enrollment từ file Excel.
     * Cột A: email, Cột B: full name, Cột C: phone (optional), Cột D: fee (optional)
     * Dòng đầu tiên là header, bỏ qua.
     * Trả về số rows import thành công.
     */
    @Override
    @Transactional
    public int importFromExcel(Long courseId, MultipartFile file) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy khóa học id=" + courseId));

        int count = 0;
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            ImportLayout layout = detectImportLayout(sheet, dataFormatter, formulaEvaluator);
            for (int i = layout.dataStartRow(); i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String email = getCellString(row, layout.emailCol(), dataFormatter, formulaEvaluator);
                String name = getCellString(row, layout.nameCol(), dataFormatter, formulaEvaluator);
                String phone = getCellString(row, layout.phoneCol(), dataFormatter, formulaEvaluator);
                String feeStr = getCellString(row, layout.feeCol(), dataFormatter, formulaEvaluator);

                if (email == null || email.isBlank()) continue;
                email = email.trim().toLowerCase();

                // Chỉ import cho user đã tồn tại để tránh tạo account rác
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    log.warn("Skip import row {}: user email '{}' không tồn tại", i + 1, email);
                    continue;
                }

                // Bỏ qua nếu đã enroll rồi
                BigDecimal fee = parseFee(feeStr);

                var existing = enrollmentRepository.findByUser_IdAndCourse_Id(user.getId(), courseId);
                if (existing.isPresent()) {
                    Enrollment e = existing.get();
                    if (name != null && !name.isBlank()) e.setFullName(name);
                    e.setEmail(email);
                    if (phone != null && !phone.isBlank()) e.setPhone(phone);
                    if (fee != null) e.setFee(fee);
                    enrollmentRepository.save(e);
                    count++;
                    continue;
                }

                Enrollment e = new Enrollment();
                e.setCourse(course);
                e.setUser(user);
                e.setFullName(name != null && !name.isBlank() ? name : email);
                e.setEmail(email);
                e.setPhone(phone);
                e.setStatus(EnrollmentStatus.PENDING);
                e.setFee(fee != null ? fee : BigDecimal.ZERO);
                e.setProgress(BigDecimal.ZERO);

                enrollmentRepository.save(e);
                count++;
            }
        }
        log.info("Import enrollment: courseId={}, inserted={}", courseId, count);
        return count;
    }

    @Override
    public List<User> findFilterUsers() {
        return enrollmentRepository.findDistinctUsersForFilter();
    }

    @Override
    public List<User> findFilterUsersByInstructor(Long instructorId) {
        return enrollmentRepository.findDistinctUsersForFilterByInstructor(instructorId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Enrollment getOrThrow(Long id) {
        return enrollmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy enrollment id=" + id));
    }

    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException("Username hoặc email không được để trống.");
        }
        return userRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng: " + usernameOrEmail));
    }

    private EnrollmentListItemDTO toListItemDTO(Enrollment e) {
        return EnrollmentListItemDTO.builder()
                .id(e.getId())
                .courseId(e.getCourse().getId())
                .courseTitle(e.getCourse().getTitle())
                .fee(e.getFee())
                .userId(e.getUser().getId())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .enrollDate(e.getEnrollDate())
                .status(e.getStatus())
                .progress(e.getProgress())
                .build();
    }

    private EnrollmentFormDTO toFormDTO(Enrollment e) {
        return EnrollmentFormDTO.builder()
                .id(e.getId())
                .courseId(e.getCourse().getId())
                .courseTitle(e.getCourse().getTitle())
                .fee(e.getFee())
                .usernameOrEmail(e.getUser().getEmail())
                .fullName(e.getFullName())
                .enrollNote(e.getEnrollNote())
                .status(e.getStatus())
                .rejectedNotes(e.getRejectedNotes())
                .progress(e.getProgress())
                .completedAt(e.getCompletedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private String csvEscape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private BigDecimal parseFee(String feeStr) {
        if (feeStr == null || feeStr.isBlank()) return BigDecimal.ZERO;

        String raw = feeStr.trim().replaceAll("[^0-9,.-]", "");
        if (raw.isBlank()) return BigDecimal.ZERO;

        // Handle common decimal formats: 1,234.56 | 1.234,56 | 1234,56
        if (raw.contains(",") && raw.contains(".")) {
            if (raw.lastIndexOf(',') > raw.lastIndexOf('.')) {
                raw = raw.replace(".", "").replace(',', '.');
            } else {
                raw = raw.replace(",", "");
            }
        } else if (raw.contains(",")) {
            raw = raw.replace(',', '.');
        }

        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private ImportLayout detectImportLayout(Sheet sheet,
                                            DataFormatter formatter,
                                            FormulaEvaluator evaluator) {
        int headerRowIndex = -1;
        Map<String, Integer> indexMap = new HashMap<>();

        int scanUntil = Math.min(sheet.getLastRowNum(), 5);
        for (int r = 0; r <= scanUntil; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            Map<String, Integer> currentMap = new HashMap<>();
            for (Cell cell : row) {
                String raw = formatter.formatCellValue(cell, evaluator);
                String normalized = normalizeHeader(raw);
                if (normalized.isBlank()) continue;

                if (normalized.contains("email")) currentMap.put("email", cell.getColumnIndex());
                if (normalized.contains("fullname") || normalized.equals("name") || normalized.contains("hoten")) {
                    currentMap.put("name", cell.getColumnIndex());
                }
                if (normalized.contains("phone") || normalized.contains("sdt") || normalized.contains("phonenumber")) {
                    currentMap.put("phone", cell.getColumnIndex());
                }
                if (normalized.contains("fee") || normalized.contains("hocphi") || normalized.contains("price")) {
                    currentMap.put("fee", cell.getColumnIndex());
                }
            }

            if (currentMap.containsKey("email")) {
                headerRowIndex = r;
                indexMap = currentMap;
                break;
            }
        }

        int emailCol = indexMap.getOrDefault("email", 0);
        int nameCol = indexMap.getOrDefault("name", 1);
        int phoneCol = indexMap.getOrDefault("phone", 2);
        int feeCol = indexMap.getOrDefault("fee", 3);

        int firstDataRow = 0;
        int lastRow = sheet.getLastRowNum();
        for (int r = 0; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            boolean hasAnyValue = false;
            for (Cell cell : row) {
                String v = formatter.formatCellValue(cell, evaluator);
                if (v != null && !v.trim().isBlank()) {
                    hasAnyValue = true;
                    break;
                }
            }
            if (hasAnyValue) {
                firstDataRow = r;
                break;
            }
        }

        int dataStartRow = headerRowIndex >= 0 ? headerRowIndex + 1 : firstDataRow;

        return new ImportLayout(emailCol, nameCol, phoneCol, feeCol, dataStartRow);
    }

    private String normalizeHeader(String value) {
        if (value == null) return "";
        String noAccent = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccent.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private String getCellString(Row row, int col,
                                 DataFormatter dataFormatter,
                                 FormulaEvaluator formulaEvaluator) {
        if (col < 0) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String value = dataFormatter.formatCellValue(cell, formulaEvaluator);
        return value != null ? value.trim() : null;
    }

    private record ImportLayout(int emailCol, int nameCol, int phoneCol, int feeCol, int dataStartRow) {}
}