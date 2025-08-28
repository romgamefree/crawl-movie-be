package com.devteria.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Country extends BaseEntity{
    @Column(nullable = false)
    String name;

    @Column(length = 8)
    String code; // ví dụ: VN, US

    @Column(unique = true, nullable = false)
    String slug;
}


