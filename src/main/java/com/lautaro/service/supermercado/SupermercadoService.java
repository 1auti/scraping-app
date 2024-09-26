package com.lautaro.service.supermercado;

import com.lautaro.entitiy.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.formula.functions.T;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;

public interface SupermercadoService {

    //Obtener por...
    void obtenerSupermercados();
    List<Categoria> obtenerCategorias();
    List<Subcategoria> obtenerSubcategorias();
    List<Tipo> obtenerTipo();

    //Buscar por...
    List<Producto> obtenerProductosPorSupermercado(String supermercado);
    //List<Producto> buscarProductosPorPrecio(double precionMin, double precioMax);
    List<Producto> buscarProductosPorMarca(String marca);

    //Eliminar por...

    //Editar por...

    //Crear Excel...
    void generarExcelPorCategoria(List<HistorialPrecios> historialPrecios,String categoria) throws IOException;
    void generarExcelPorSubcategoria(List<HistorialPrecios> historialPrecios,String subcategoria);
    void generarExcelPorTipo(List<HistorialPrecios> historialPrecios,String tipo);











}
