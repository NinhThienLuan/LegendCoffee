package fpt.legendcoffee.entity;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(name = "category_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String categoryName;

    @Column(name = "description", columnDefinition = "NVARCHAR(255)")
    private String description;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private Set<Product> products = new LinkedHashSet<>();

}