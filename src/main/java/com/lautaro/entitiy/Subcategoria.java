package com.lautaro.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Subcategoria")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subcategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String link;

    @OneToMany(mappedBy = "subcategoria", cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
    private List<Tipo> tipoList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}
