package kz.nutrifit.nutrition.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "meal_items")
public class MealItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    @JsonBackReference
    private Meal meal;

    @Column(nullable = false)
    private String name;

    @Column(name = "calories_per_100g")
    private Double caloriesPer100g;

    private Double protein;
    private Double carbs;
    private Double fat;

    @Column(nullable = false)
    private Double quantity;
}
