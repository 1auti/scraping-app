package com.lautaro.entitiy;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "valor_nutricional")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValorNutricional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "nutrientes", joinColumns = @JoinColumn(name = "valor_nutricional_id"))
    @MapKeyColumn(name = "nutriente")
    @Column(name = "valor")
    private Map<String, String> nutrientes = new HashMap<>();

    @OneToOne(mappedBy = "valorNutricional", fetch = FetchType.EAGER)
    private Producto producto;
}

