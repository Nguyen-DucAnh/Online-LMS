package com.online.lms.entity;

import com.online.lms.enums.LessonType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lesson")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson extends BaseEntity {

    @Column(name = "title", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "NVARCHAR(20)")
    private LessonType type;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "status")
    @Builder.Default
    private Boolean status = true;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(name = "content_file_path", columnDefinition = "NVARCHAR(500)")
    private String contentFilePath;


    @Column(name = "preview_enabled", nullable = false)
    @Builder.Default
    private Boolean previewEnabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;
}
