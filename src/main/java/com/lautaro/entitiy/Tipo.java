package com.lautaro.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Tipo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String link;

    @OneToMany(mappedBy = "tipo", cascade = CascadeType.ALL, fetch = FetchType.EAGER , orphanRemoval = true)
    private List<Producto> productoList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subcategoria_id", nullable = false)
    private Subcategoria subcategoria;
}
