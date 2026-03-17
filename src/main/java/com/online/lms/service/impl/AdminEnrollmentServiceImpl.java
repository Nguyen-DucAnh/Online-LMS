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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEnrollmentServiceImpl implements AdminEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository     courseRepository;
    private final UserRepository       userRepository;

    // ── List ──────────────────────────────────────────────────────────────────

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
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String email   = getCellString(row, 0);
                String name    = getCellString(row, 1);
                String phone   = getCellString(row, 2);
                String feeStr  = getCellString(row, 3);

                if (email == null || email.isBlank()) continue;

                // Tìm hoặc tạo user
                User user = userRepository.findByEmail(email)
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setEmail(email);
                            newUser.setFullName(name != null ? name : email);
                            newUser.setPhone(phone);
                            newUser.setPassword("IMPORT_" + System.currentTimeMillis());
                            newUser.setStatus(com.online.lms.enums.UserStatus.PENDING);
                            newUser.setRole(com.online.lms.enums.UserRole.MEMBER);
                            return userRepository.save(newUser);
                        });

                // Bỏ qua nếu đã enroll rồi
                if (enrollmentRepository.findByUser_IdAndCourse_Id(user.getId(), courseId).isPresent()) {
                    continue;
                }

                BigDecimal fee = BigDecimal.ZERO;
                try { if (feeStr != null && !feeStr.isBlank()) fee = new BigDecimal(feeStr); }
                catch (NumberFormatException ignored) {}

                // Enrollment entity không có @Builder
                Enrollment e = new Enrollment();
                e.setCourse(course);
                e.setUser(user);
                e.setFullName(name != null ? name : email);
                e.setEmail(email);
                e.setPhone(phone);
                e.setStatus(EnrollmentStatus.PENDING);
                e.setFee(fee);
                e.setProgress(BigDecimal.ZERO);

                enrollmentRepository.save(e);
                count++;
            }
        }
        log.info("Import enrollment: courseId={}, inserted={}", courseId, count);
        return count;
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

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default      -> null;
        };
    }
}