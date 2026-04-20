package fpt.legendcoffee.entity;

import fpt.legendcoffee.common.infrastructure.BaseEntity;
import fpt.legendcoffee.entity.enumeration.PromotionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "promotions")
public class Promotion extends BaseEntity {

    @Column(name = "promotion_type")
    private String type;

    @Column(name = "promotion_value", precision = 18)
    private BigDecimal value;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;


}