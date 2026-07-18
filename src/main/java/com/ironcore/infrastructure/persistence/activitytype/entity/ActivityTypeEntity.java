package com.ironcore.infrastructure.persistence.activitytype.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
