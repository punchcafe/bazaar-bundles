package com.example.codingexercise.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PackageProductId {
    @Column(name = "package_id") private Long packageId;
    @Column(name = "product_id") private String productId;
}
