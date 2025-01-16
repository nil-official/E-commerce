package com.ecommerce.model;

import com.ecommerce.user.domain.ProductSize;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Size {

    @Enumerated(EnumType.STRING) // Ensures the enum is stored as a string
    private ProductSize name;

    private int quantity;

}
