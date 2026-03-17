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

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "value", length = 100)
    private String value;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "status")
    private String status;

    @Column(name = "description", length = 200)
    private String description;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private Setting type;
    
    public boolean isActive() {
        return "Active".equalsIgnoreCase(this.status);
    }
}