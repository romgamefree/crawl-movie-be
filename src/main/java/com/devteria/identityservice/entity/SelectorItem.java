package com.devteria.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import com.devteria.identityservice.constant.SelectorAttribute;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class SelectorItem extends BaseEntity{
    @ManyToOne(optional = false)
    @JoinColumn(name = "selector_id")
    Selector selector;

    // Tên của item, ví dụ: "title", "episode", "link"
    @Column(nullable = false)
    String name;

    // CSS/JSoup selector để lấy giá trị của item này
    @Column(nullable = false)
    String query;

    // Nếu cần lấy attribute thay vì text()
    String attribute;

    // Ghi chú cho item này
    String note;
    
    /**
     * Nếu true: lấy tất cả kết quả (selectAll)
     * Nếu false: chỉ lấy phần tử đầu tiên (selectFirst)
     */
    @Builder.Default
    Boolean isList = false;
}
