package com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity;

import com.ironcore.infrastructure.persistence.muscle.musclegroup.entity.MuscleGroupEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "muscle_subgroups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MuscleSubgroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "muscle_group_id", nullable = false)
    private MuscleGroupEntity muscleGroup;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
