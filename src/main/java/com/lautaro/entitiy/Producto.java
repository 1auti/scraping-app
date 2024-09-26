    package com.lautaro.entitiy;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.hibernate.annotations.Type;

    import java.time.LocalDate;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @Entity
    @Table(name = "Producto")
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public class Producto {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;
        private String nombre;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "tipo_id")
        private Tipo tipo;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "supermercado_id")
        private Supermercado supermercado;

        @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
        private List<Certificado> certificadoList = new ArrayList<>();

        @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
        private List<HistorialPrecios> historialPrecios = new ArrayList<>();

        private String marca;
        @Column(length = 500)
        private String descripcion;

        @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL,  fetch = FetchType.EAGER,orphanRemoval = true)
        private List<Imagen> imagenes = new ArrayList<>();

        private String link;

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "producto_especificaciones", joinColumns = @JoinColumn(name = "producto_id"))
        @MapKeyColumn(name = "spec_key")
        @Column(name = "spec_value")
        private Map<String, String> especificaciones = new HashMap<>();

        private String ingredientes;
        private LocalDate fechaIngreso;

        @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
        @JoinColumn(name = "valor_nutricional_id")
        private ValorNutricional valorNutricional;
    }