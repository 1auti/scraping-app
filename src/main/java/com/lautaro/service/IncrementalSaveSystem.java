package com.lautaro.service;

import com.lautaro.entitiy.*;
import com.lautaro.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class IncrementalSaveSystem {

    private static final Logger logger = LoggerFactory.getLogger(IncrementalSaveSystem.class);
    private final SupermercadoRepository supermercadoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final TipoRepository tipoRepository;
    private final ProductoRepository productoRepository;
    private final HistorialPrecioRepository historialPrecioRepository;
    private final ImagenRepository imagenRepository;
    private final CertificadoRepository certificadoRepository;
    private final ValorNutricionalRepository valorNutricionalRepository;

    public Supermercado saveSupermercado(Supermercado supermercado) {
        return supermercadoRepository.save(supermercado);
    }

    public Categoria saveCategoria(Categoria categoria) {
        if (categoria.getSupermercado() == null || categoria.getSupermercado().getId() == null) {
            throw new IllegalStateException("La categoría debe tener un supermercado asociado con un ID válido");
        }
        return categoriaRepository.save(categoria);
    }

    public Subcategoria saveSubcategoria(Subcategoria subcategoria) {
        if (subcategoria.getCategoria() == null || subcategoria.getCategoria().getId() == null) {
            throw new IllegalStateException("La subcategoria debe tener un categoria asociado con un ID válido");
        }
        return subcategoriaRepository.save(subcategoria);
    }

    public Tipo saveTipo(Tipo tipo) {
        if (tipo.getSubcategoria() == null || tipo.getSubcategoria().getId() == null) {
            throw new IllegalStateException("El tipo  debe tener un subcategoria asociado con un ID válido");
        }
        // Verifica si el objeto subcategoria tiene un id válido
        if (subcategoriaRepository.findById(tipo.getSubcategoria().getId()).isEmpty()) {
            throw new IllegalStateException("La subcategoria no existe en la base de datos");
        }
        return tipoRepository.save(tipo);
    }
    public Producto saveProducto(Producto producto) {
        if (producto.getTipo() == null || producto.getTipo().getId() == null) {
            throw new IllegalStateException("El producto debe tener un tipo asociado con un ID válido");
        }
        if (producto.getSupermercado() == null || producto.getSupermercado().getId() == null) {
            throw new IllegalStateException("El producto debe tener un supermercado asociado con un ID válido");
        }
        try {
            Producto savedProducto = productoRepository.save(producto);

            // Save related entities
            if (producto.getHistorialPrecios() != null) {
                producto.getHistorialPrecios().forEach(hp -> saveHistorialPrecio(hp, savedProducto));
            }
            if (producto.getImagenes() != null) {
                producto.getImagenes().forEach(img -> saveImagen(img, savedProducto));
            }
            if (producto.getCertificadoList() != null) {
                producto.getCertificadoList().forEach(cert -> saveCertificado(cert, savedProducto));
            }
            if (producto.getValorNutricional() != null) {
                saveValorNutricional(producto.getValorNutricional(), savedProducto);
            }

            return savedProducto;
        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integridad de datos al guardar el producto: {}", producto.getNombre(), e);
            throw new RuntimeException("Error al guardar el producto debido a violación de integridad de datos", e);
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("Error de concurrencia al guardar el producto: {}", producto.getNombre(), e);
            throw new RuntimeException("Error al guardar el producto debido a un conflicto de concurrencia", e);
        } catch (Exception e) {
            logger.error("Error inesperado al guardar el producto: {}", producto.getNombre(), e);
            throw new RuntimeException("Error inesperado al guardar el producto", e);
        }
    }
    public HistorialPrecios saveHistorialPrecio(HistorialPrecios historialPrecios, Producto producto) {
        historialPrecios.setProducto(producto);
        return historialPrecioRepository.save(historialPrecios);
    }

    public Imagen saveImagen(Imagen imagen, Producto producto) {
        imagen.setProducto(producto);
        return imagenRepository.save(imagen);
    }

    public Certificado saveCertificado(Certificado certificado, Producto producto) {
        certificado.setProducto(producto);
        return certificadoRepository.save(certificado);
    }

    public ValorNutricional saveValorNutricional(ValorNutricional valorNutricional, Producto producto) {
        valorNutricional.setProducto(producto);
        return valorNutricionalRepository.save(valorNutricional);
    }
}