package com.kabaev.shop.service.keeper.domain;

import lombok.*;

import javax.persistence.*;

//@ToString
//@Getter
//@Setter
//@EqualsAndHashCode
//@NoArgsConstructor
//@AllArgsConstructor
@Data
@Entity
@Table
public class Product {

    @Id
    @SequenceGenerator(
            name = "product_sequence",
            sequenceName = "product_sequence"
    )
    @GeneratedValue(
            generator = "student_sequence",
            strategy = GenerationType.SEQUENCE
    )
    private Long id;
    private String name;

    public Product(String name) {
        this.name = name;
    }

}
