package com.lautaro.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "Supermercado")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Supermercado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;

    @OneToMany(mappedBy = "supermercado", fetch = FetchType.EAGER,orphanRemoval = true)
    private List<Producto> productos = new ArrayList<>();

    @OneToMany(mappedBy = "supermercado", cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
    private List<Categoria> categorias = new ArrayList<>();
}

