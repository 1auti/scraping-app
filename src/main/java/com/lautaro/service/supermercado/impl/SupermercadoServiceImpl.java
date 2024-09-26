package com.lautaro.service.supermercado.impl;

import com.lautaro.entitiy.*;
import com.lautaro.repository.CategoriaRepository;
import com.lautaro.repository.ProductoRepository;
import com.lautaro.repository.SubcategoriaRepository;
import com.lautaro.repository.TipoRepository;
import com.lautaro.service.scraping.CotoService;
import com.lautaro.service.scraping.DiaService;
import com.lautaro.service.scraping.JumboService;
import com.lautaro.service.supermercado.SupermercadoService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.lautaro.service.WebDriverConfig.configurarWebDriver;

@Service
@RequiredArgsConstructor
public class SupermercadoServiceImpl  implements SupermercadoService {

    private final CotoService cotoService;
    private final JumboService jumboService;
    private final DiaService diaService;

    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final TipoRepository tipoRepository;
    private final ProductoRepository productoRepository;



    @Override
    public void obtenerSupermercados() {

        List<CompletableFuture<Supermercado>> futures = new ArrayList<>();

        futures.add(CompletableFuture.supplyAsync(() -> {
            try {
                return jumboService.obtenerTodaLaInformacionJumbo();
            } catch (Exception e) {
                System.err.println("Error obteniendo información de Jumbo: " + e.getMessage());
                return null;
            }
        }));

        futures.add(CompletableFuture.supplyAsync(() -> {
            try {
                return cotoService.obtenerTodaLaInformacionCoto();
            } catch (Exception e) {
                System.err.println("Error obteniendo información de Coto: " + e.getMessage());
                return null;
            }
        }));

        futures.add(CompletableFuture.supplyAsync(() -> {
            try {
                return diaService.obtenerTodaLaInformacionDia();
            } catch (Exception e) {
                System.err.println("Error obteniendo información de Dia: " + e.getMessage());
                return null;
            }
        }));

    }

    //Obtener por...
    @Override
    public List<Categoria> obtenerCategorias() {
        return categoriaRepository.findAll();
    }

    @Override
    public List<Subcategoria> obtenerSubcategorias() {
        return subcategoriaRepository.findAll();
    }

    @Override
    public List<Tipo> obtenerTipo() {
        return tipoRepository.findAll();
    }



    @Override
    public List<Producto> obtenerProductosPorSupermercado(String supermercado) {
        return productoRepository.findAll().stream()
                .filter(p -> p.getSupermercado().getNombre().equals(supermercado))
                .collect(Collectors.toList());
    }



    @Override
    public List<Producto> buscarProductosPorMarca(String marca) {
        return productoRepository.findByMarca(marca);
    }




    //Generar Excel
    private static final String RUTA_BASE = "C:\\Users\\Lautaro\\IdeaProjects\\scraping-app\\excel\\";
    private static final String RUTA_CATEGORIA = RUTA_BASE + "Categoria\\";
    private static final String RUTA_SUBCATEGORIA = RUTA_BASE + "Subcategoria\\";
    private static final String RUTA_TIPO = RUTA_BASE + "Tipo\\";
    private static final String NOMBRE_ARCHIVO = "productos_%s.xlsx";



    private Workbook crearWorkbook() {
        return new XSSFWorkbook();
    }

    private void crearCabecera(Row row) {
        row.createCell(0).setCellValue("ID");
        row.createCell(1).setCellValue("Nombre");
        row.createCell(2).setCellValue("Descripción");
        row.createCell(3).setCellValue("Precio");
        row.createCell(4).setCellValue("Supermercado");
        row.createCell(5).setCellValue("Categoría");
        row.createCell(6).setCellValue("Subcategoría");
        row.createCell(7).setCellValue("Tipo");
    }

    private void crearFila(Row row, Producto producto) {
        row.createCell(0).setCellValue(producto.getId());
        row.createCell(1).setCellValue(producto.getNombre());
        row.createCell(2).setCellValue(producto.getDescripcion());
        int tamanio = producto.getHistorialPrecios().size() -1;
        row.createCell(3).setCellValue(producto.getHistorialPrecios().get(tamanio).getPrecio());
        row.createCell(4).setCellValue(producto.getSupermercado().getNombre());
        row.createCell(5).setCellValue(producto.getSupermercado().getCategorias().get(0).getNombre());
        row.createCell(6).setCellValue(producto.getSupermercado().getCategorias().get(0).getSubcategorias().get(0).getNombre());
        row.createCell(7).setCellValue(producto.getSupermercado().getCategorias().get(0).getSubcategorias().get(0).getTipoList().get(0).getNombre());
    }

    @Override
    public void generarExcelPorCategoria(List<HistorialPrecios> historialPrecios, String categoria) {
        String rutaGuardado = String.format(RUTA_CATEGORIA + NOMBRE_ARCHIVO, categoria);
        try (FileOutputStream outputStream = new FileOutputStream(rutaGuardado)) {
            Workbook workbook = crearWorkbook();
            Sheet sheet = workbook.createSheet("Productos");
            Row row = sheet.createRow(0);
            crearCabecera(row);

            historialPrecios.sort(Comparator.comparing((HistorialPrecios hp) -> hp.getProducto().getHistorialPrecios().stream()
                    .max(Comparator.comparing(HistorialPrecios::getFechaPrecio)).get().getPrecio()));

            int rowIndex = 1;
            for (HistorialPrecios historialPrecio : historialPrecios) {
                row = sheet.createRow(rowIndex);
                crearFila(row, historialPrecio.getProducto());
                rowIndex++;
            }

            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar archivo Excel", e);
        }
    }
    @Override
    public void generarExcelPorSubcategoria(List<HistorialPrecios> historialPrecios, String subcategoria) {
        String rutaGuardado = String.format(RUTA_SUBCATEGORIA + "Subcategorias\\" + NOMBRE_ARCHIVO, subcategoria);
        try (FileOutputStream outputStream = new FileOutputStream(rutaGuardado)) {
            Workbook workbook = crearWorkbook();
            Sheet sheet = workbook.createSheet("Productos");
            Row row = sheet.createRow(0);
            crearCabecera(row);

            historialPrecios.sort(Comparator.comparing((HistorialPrecios hp) -> hp.getProducto().getHistorialPrecios().stream()
                    .max(Comparator.comparing(HistorialPrecios::getFechaPrecio)).get().getPrecio()));

            int rowIndex = 1;
            for (HistorialPrecios historialPrecio : historialPrecios) {
                if (historialPrecio.getProducto().getSupermercado().getCategorias().stream()
                        .anyMatch(categoria -> categoria.getSubcategorias().stream()
                                .anyMatch(subcategoriaProducto -> subcategoriaProducto.getNombre().equals(subcategoria)))) {
                    row = sheet.createRow(rowIndex);
                    crearFila(row, historialPrecio.getProducto());
                    rowIndex++;
                }
            }

            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar archivo Excel", e);
        }
    }

    @Override
    public void generarExcelPorTipo(List<HistorialPrecios> historialPrecios, String tipo) {
        String rutaGuardado = String.format(RUTA_TIPO  + NOMBRE_ARCHIVO, tipo);
        try (FileOutputStream outputStream = new FileOutputStream(rutaGuardado)) {
            Workbook workbook = crearWorkbook();
            Sheet sheet = workbook.createSheet("Productos");
            Row row = sheet.createRow(0);
            crearCabecera(row);

            historialPrecios.sort(Comparator.comparing((HistorialPrecios hp) -> hp.getProducto().getHistorialPrecios().stream()
                    .max(Comparator.comparing(HistorialPrecios::getFechaPrecio)).get().getPrecio()));

            int rowIndex = 1;
            for (HistorialPrecios historialPrecio : historialPrecios) {
                if (historialPrecio.getProducto().getSupermercado().getCategorias().stream()
                        .anyMatch(categoria -> categoria.getSubcategorias().stream()
                                .anyMatch(subcategoriaProducto -> subcategoriaProducto.getTipoList().stream()
                                        .anyMatch(tipoProducto -> tipoProducto.getNombre().equals(tipo))))) {
                    row = sheet.createRow(rowIndex);
                    crearFila(row, historialPrecio.getProducto());
                    rowIndex++;
                }
            }

            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar archivo Excel", e);
        }
    }





}
