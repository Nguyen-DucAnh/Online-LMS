package com.online.lms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "setting", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "type_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting extends BaseEntity {

    @Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(20)")
    private String name;

    @Column(name = "value", columnDefinition = "NVARCHAR(100)")
    private String value;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "status", columnDefinition = "NVARCHAR(50)")
    private String status;

    @Column(name = "description", columnDefinition = "NVARCHAR(200)")
    private String description;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private Setting type;
    
    public boolean isActive() {
        return "Active".equalsIgnoreCase(this.status);
    }
}