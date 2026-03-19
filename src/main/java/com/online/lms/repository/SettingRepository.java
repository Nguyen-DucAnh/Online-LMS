package com.online.lms.repository;

import com.online.lms.entity.Setting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

    @Query("SELECT s FROM Setting s WHERE " +
            "(:typeId IS NULL OR s.type.id = :typeId) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:keyword IS NULL OR s.name LIKE %:keyword% OR s.value LIKE %:keyword%)")
    Page<Setting> findAllCustom(
            @Param("typeId") Long typeId,
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable);
}