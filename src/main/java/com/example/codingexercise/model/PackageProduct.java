package com.example.codingexercise.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="package_products")
@Data
public class PackageProduct {

    @EmbeddedId
    private PackageProductId id;
}
