package com.lautaro.service.scraping.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lautaro.entitiy.*;
import com.lautaro.repository.ProductoRepository;
import com.lautaro.service.IncrementalSaveSystem;
import com.lautaro.service.scraping.CotoService;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;


import static com.lautaro.service.WebDriverConfig.configurarWebDriver;

@Service
@RequiredArgsConstructor
public class CotoServiceImpl implements CotoService {

    private static final Logger logger = LoggerFactory.getLogger(CotoServiceImpl.class);
    private static final String COTO_BASE_URL = "https://www.cotodigital3.com.ar";
    private static final String PRODUCT_NAME_SELECTOR = "h1.product_page";
    private final IncrementalSaveSystem incrementalSaveSystem;
    private final ProductoRepository productoRepository;


    public Supermercado obtenerTodaLaInformacionCoto() {
        Supermercado supermercadoCoto = new Supermercado();
        supermercadoCoto.setNombre("Coto");
        supermercadoCoto = incrementalSaveSystem.saveSupermercado(supermercadoCoto);
        WebDriver driver = null;

        try {
            driver = configurarWebDriver();
            driver.get(COTO_BASE_URL);
            Thread.sleep(2000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("li.atg_store_dropDownParent")));

            List<WebElement> liElements = driver.findElements(By.cssSelector("li.atg_store_dropDownParent"));
            logger.info("Iniciando metodo para obtener categorias...");
            for (WebElement liElement : liElements) {
                try {
                    WebElement linkElement = liElement.findElement(By.tagName("a"));
                    String linkText = linkElement.getText().trim();
                    String linkHref = linkElement.getAttribute("href");

                    if (!linkText.isEmpty() && !linkHref.isEmpty()) {
                        Categoria categoriaAux = new Categoria();
                        categoriaAux.setNombre(linkText);
                        categoriaAux.setLink(linkHref);
                        categoriaAux.setSupermercado(supermercadoCoto);
                        logger.info("Categoria {}  Link: {}",categoriaAux.getNombre(),categoriaAux.getLink());
                        categoriaAux = incrementalSaveSystem.saveCategoria(categoriaAux);
                        //Iniciamos el metodo para obtener las subcategorias
                        categoriaAux.setSubcategorias(obtenerTipos(categoriaAux,supermercadoCoto));
                        //Después de obtener las subcategorias guardamos la categoria
                        categoriaAux = incrementalSaveSystem.saveCategoria(categoriaAux);
                        //Vinculamos la categoria
                        supermercadoCoto.getCategorias().add(categoriaAux);
                        //Guardamos
                        supermercadoCoto = incrementalSaveSystem.saveSupermercado(supermercadoCoto);

                    }
                } catch (NoSuchElementException e) {
                    logger.error("No se pudo encontrar el enlace en un elemento li: {}", e.getMessage());
                }
            }




        } catch (Exception e) {
            logger.error("Error durante el scraping de categorías: {}", e.getMessage(), e);
            return null;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return supermercadoCoto;
    }

//    @Override
//    public void actualizarPreciosCoto() {
//        List<Producto> productos = productoRepository.findBySupermercado("Coto");
//
//        for (Producto producto : productos) {
//            try {
//                WebDriver driver = configurarWebDriver();
//                driver.get(producto.getLink());
//                HistorialPrecios historialPreciosActual = obtenerPrecio(driver, producto);
//                if (!Objects.equals(historialPreciosActual.getPrecio(), producto.getHistorialPrecios().get(producto.getHistorialPrecios().size() - 1).getPrecio())) {
//                    producto.getHistorialPrecios().add(historialPreciosActual);
//                }
//                driver.quit();
//            } catch (Exception e) {
//                // Manejo de la excepción
//                System.err.println("Error al actualizar el precio del producto: " + e.getMessage());
//            }
//        }
//    }



    public List<Subcategoria> obtenerTipos(Categoria categoria, Supermercado supermercado) {

        logger.info("Iniciando metodo para obtener subcategorias...");
        WebDriver driver = configurarWebDriver();
        List<Subcategoria> subcategoriaList = new ArrayList<>();

        try {
            driver.get(COTO_BASE_URL);
            Thread.sleep(2000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

            List<Map<String, Object>> categories = (List<Map<String, Object>>) ((JavascriptExecutor) driver).executeScript(
                    "var categories = [];" +
                            "document.querySelectorAll('div.g1').forEach(function(g1) {" +
                            "  var mainCategory = g1.querySelector('h2 > a');" +
                            "  if (mainCategory) {" +
                            "    var category = {" +
                            "      name: mainCategory.textContent.replace(/\\(\\+\\)/, '').trim()," +
                            "      url: mainCategory.getAttribute('href')," +
                            "      subcategories: []" +
                            "    };" +
                            "    var subcategoriesDiv = g1.querySelector('div[id^=\"thrd_level_cat\"]');" +
                            "    if (subcategoriesDiv) {" +
                            "      subcategoriesDiv.querySelectorAll('li > a').forEach(function(subLink) {" +
                            "        category.subcategories.push({" +
                            "          name: subLink.textContent.trim()," +
                            "          url: subLink.getAttribute('href')" +
                            "        });" +
                            "      });" +
                            "    }" +
                            "    categories.push(category);" +
                            "  }" +
                            "});" +
                            "return categories;"
            );

            for (Map<String, Object> category : categories) {
                Subcategoria subcategoria = new Subcategoria();
                subcategoria.setNombre(category.get("name").toString());
                subcategoria.setLink(category.get("url").toString());
                subcategoria.setCategoria(categoria);
                subcategoria = incrementalSaveSystem.saveSubcategoria(subcategoria);
                logger.info("Subcategoria :  {}  Link : {} ",subcategoria.getNombre(),subcategoria.getLink());
                List<Tipo> tipoList = new ArrayList<>();
                List<Map<String, String>> subcategories = (List<Map<String, String>>) category.get("subcategories");
                logger.info("Iniciando metedo para obtener tipos...");

                for (Map<String, String> subcategory : subcategories) {
                    Tipo tipo = new Tipo();
                    tipo.setNombre(subcategory.get("name"));
                    tipo.setLink(subcategory.get("url"));
                    tipo.setSubcategoria(subcategoria);
                    tipo = incrementalSaveSystem.saveTipo(tipo);
                    logger.info("Tipo {} Link {}",tipo.getNombre(), tipo.getLink());
                    tipo.setProductoList(obtenerProductos(tipo,supermercado,categoria,subcategoria));
                    tipo = incrementalSaveSystem.saveTipo(tipo);
                    tipoList.add(tipo);
                }
                subcategoria.setTipoList(tipoList);
                subcategoria = incrementalSaveSystem.saveSubcategoria(subcategoria);
                subcategoriaList.add(subcategoria);
            }

        } catch (Exception e) {
            logger.error("Error al obtener tipos: {}", e.getMessage(), e);
        }finally {
            driver.quit();
        }
        return subcategoriaList;
    }

    public List<Producto> obtenerProductos(Tipo tipo, Supermercado supermercado, Categoria categoria, Subcategoria subcategoria) {
        logger.info("Iniciando metodo para obtener Productos...");
        WebDriver driver = configurarWebDriver();
        List<Producto> productos = new ArrayList<>();

        try {
            String URL = COTO_BASE_URL + tipo.getLink();
            driver.get(URL);
            Thread.sleep(2000);

            List<WebElement> productElements = driver.findElements(By.cssSelector("div.product_info_container"));

            for (WebElement productElement : productElements) {
                WebElement linkElement = productElement.findElement(By.cssSelector("a"));
                String linkProducto = linkElement.getAttribute("href");
                Producto producto = new Producto();
                producto.setLink(linkProducto);
                producto.setTipo(tipo);
                producto.setSupermercado(supermercado);
                producto = incrementalSaveSystem.saveProducto(producto);
                producto = obtenerProducto(producto,tipo,supermercado,categoria,subcategoria);
                logger.info("Nombre: {} Link {}",producto.getNombre(),producto.getLink());
                producto = incrementalSaveSystem.saveProducto(producto);
                supermercado.getProductos().add(producto);
                supermercado = incrementalSaveSystem.saveSupermercado(supermercado);
                productos.add(producto);
            }

        } catch (Exception e) {
            logger.error("Error al obtener productos: {}", e.getMessage(), e);
        }finally {
            driver.quit();
        }
        return productos;
    }

    public Producto obtenerProducto(Producto producto, Tipo tipo , Supermercado supermercado, Categoria categoria,Subcategoria subcategoria) {
        logger.info("Iniciar metodo para obtener producto");
        WebDriver driver = configurarWebDriver();
        try {
            driver.get(producto.getLink());
            Thread.sleep(2000);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Set your Cloudinary credentials
            Dotenv dotenv = Dotenv.load();
            Cloudinary cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
            cloudinary.config.secure = true;


            String nombreCategoria = categoria.getNombre();
            String nombreSubcategoria = subcategoria.getNombre();
            String nombreTipo = tipo.getNombre();

            //Extramos el precio
            producto.getHistorialPrecios().add(obtenerPrecio(driver,producto));

            //Extraemos el nombre
            String elementoNombre = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(PRODUCT_NAME_SELECTOR))).getText();
            producto.setNombre(elementoNombre);

            WebElement descriptionElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("txtComentario")));
            String description = descriptionElement.getText();
            producto.setDescripcion(description);

            // Obtener el elemento <div> con la clase "productImageZoom"
            WebElement productImageZoom = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("productImageZoom")));


            // Obtener los elementos <img> dentro del elemento <div>
            List<WebElement> images = productImageZoom.findElements(By.tagName("img"));

            // Subir las imágenes a Cloudinary
            crearEstructuraCarpetas(cloudinary,categoria,subcategoria,tipo,producto);
            for (WebElement image : images) {
                String imgUrl = image.getAttribute("src");
                try {
                    String nombreImagen = generarNombreImagen(producto);

                    Map<String, String> options = new HashMap<>();
                    String publicId = String.format("Coto/%s/%s/%s/%s_%s_%s",
                            UUID.randomUUID().toString(),
                            nombreCategoria.replaceAll("[^a-zA-Z0-9_]", "_"),
                            nombreSubcategoria.replaceAll("[^a-zA-Z0-9_]", "_"),
                            nombreTipo.replaceAll("[^a-zA-Z0-9_]", "_"),
                            producto.getNombre().replaceAll("[^a-zA-Z0-9_]", "_"),
                            nombreImagen.replaceAll("[^a-zA-Z0-9_]", "_"));
                    options.put("public_id", publicId);

                    String folder = "supermercados/Coto/" + categoria.getNombre() + "/" + subcategoria.getNombre() + "/" + tipo.getNombre() + "/" + producto.getNombre();
                    // Sube la imagen a Cloudinary
                    Imagen imagen = uploadImage(cloudinary, imgUrl, producto,folder,nombreImagen);

                    // Agrega la URL de la imagen subida a la lista de imágenes del producto
                    producto.getImagenes().add(imagen);
                } catch (IOException e) {
                    System.err.println("Error al subir la imagen: " + imgUrl);
                    e.printStackTrace();
                }
            }

            //Extrear caracteristicas
            WebElement tablaCaracteristicas = driver.findElement(By.cssSelector("#tab1 .tblData"));
            List<WebElement> filas = tablaCaracteristicas.findElements(By.tagName("tr"));

            for (WebElement fila : filas) {
                List<WebElement> celdas = fila.findElements(By.tagName("td"));
                if (celdas.size() == 2) {
                    String clave = celdas.get(0).getText().trim();
                    String valor = celdas.get(1).getText().trim();
                    if(clave.equals("Marca")){
                        producto.setMarca(valor);
                    }else{
                        producto.getEspecificaciones().put(clave,valor);
                    }

                }
            }

            //Ingresamos la fecha de ingreso a la base de datos
            producto.setFechaIngreso(LocalDate.now());
            logger.info("Producto cargado con exito");


        } catch (TimeoutException e) {

        } catch (WebDriverException e) {

        } catch (Exception e) {

        }finally {
            driver.quit();
        }
        return producto;
    }

    private void crearEstructuraCarpetas(Cloudinary cloudinary, Categoria categoria, Subcategoria subcategoria, Tipo tipo, Producto producto) {
        String[] carpetas = {
                "supermercados",
                "supermercados/Coto",
                "supermercados/Coto/" + categoria.getNombre(),
                "supermercados/Coto/" + categoria.getNombre() + "/" + subcategoria.getNombre(),
                "supermercados/Coto/" + categoria.getNombre() + "/" + subcategoria.getNombre() + "/" + tipo.getNombre(),
                "supermercados/Coto/" + categoria.getNombre() + "/" + subcategoria.getNombre() + "/" + tipo.getNombre() + "/" + producto.getNombre()
        };

        for (String carpeta : carpetas) {
            try {
                cloudinary.api().createFolder(carpeta, ObjectUtils.emptyMap());
                System.out.println("Carpeta creada: " + carpeta);
            } catch (Exception e) {
                if (e.getMessage().contains("Folder already exists")) {
                    System.out.println("La carpeta ya existe: " + carpeta);
                } else {
                    System.out.println("Error al crear la carpeta " + carpeta + ": " + e.getMessage());
                }
            }
        }
    }

    private String generarNombreImagen(Producto producto) {
        return sanitizeString(producto.getNombre());
    }

    private String sanitizeFolderName(String folderPath) {
        if (folderPath == null || folderPath.isEmpty()) {
            throw new IllegalArgumentException("La ruta de la carpeta no puede ser null o vacía");
        }

        String[] parts = folderPath.split("/");
        StringBuilder sanitized = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) continue;

            // Sanitiza cada parte individualmente
            String sanitizedPart = part.replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-]", ""); // Mantener letras (con acentos), números, espacios y guiones
            sanitizedPart = sanitizedPart.replaceAll("\\s+", "-"); // Reemplazar espacios por guiones
            sanitizedPart = sanitizedPart.replaceAll("-+", "-"); // Eliminar guiones consecutivos

            if (!sanitized.isEmpty()) {
                sanitized.append("/");
            }
            sanitized.append(sanitizedPart);
        }

        // Limitar la longitud total de la ruta
        String result = sanitized.toString();
        if (result.length() > 100) { // Aumentamos el límite a 100 caracteres
            result = result.substring(0, 100);
        }

        if (!isValidFolderPath(result)) {
            throw new IllegalArgumentException("El nombre de la carpeta generado no es válido");
        }

        return result;
    }

    private boolean isValidFolderPath(String folderPath) {
        return folderPath != null && !folderPath.isEmpty() &&
                !folderPath.contains("?") && !folderPath.contains("&") &&
                !folderPath.contains("#") && !folderPath.contains("%") &&
                !folderPath.contains("<") && !folderPath.contains(">") &&
                !folderPath.startsWith("/") && !folderPath.endsWith("/");
    }

    private String sanitizeString(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("El input no puede ser null o vacío");
        }

        String sanitized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", ""); // Elimina acentos

        // Reemplaza caracteres no permitidos excepto letras, números, guiones y espacios
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9\\s\\-]", " ");

        // Reemplaza secuencias de espacios consecutivos por un guion
        sanitized = sanitized.replaceAll("\\s+", "-");

        // Elimina guiones al inicio y al final
        sanitized = sanitized.replaceAll("^-+|-+$", "");

        // Truncate el public_id a una longitud máxima
        sanitized = sanitized.substring(0, Math.min(sanitized.length(), 100)); // Aumentamos el límite a 100 caracteres



        return sanitized;
    }



    private Imagen uploadImage(Cloudinary cloudinary, String imageUrl, Producto producto, String folder, String nombreImagen) throws IOException {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "image");
            options.put("folder", folder);

            String sanitizedNombreImagen = sanitizeString(nombreImagen != null ? nombreImagen : generarNombreImagen(producto));

            options.put("public_id", sanitizedNombreImagen);

            Map uploadResult = cloudinary.uploader().upload(imageUrl, options);

            Imagen imagen = new Imagen();
            imagen.setPublicId(uploadResult.get("public_id").toString());
            imagen.setUrl(uploadResult.get("url").toString());
            imagen.setProducto(producto);

            return imagen;
        } catch (IOException e) {
            logger.error("Error al cargar la imagen: " + e.getMessage(), e);
            throw new RuntimeException("Error al cargar la imagen", e);
        }
    }

    private HistorialPrecios obtenerPrecio(WebDriver driver, Producto producto) {
        // Ejecutar un script JavaScript para obtener el precio
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

        // Script modificado para obtener el precio visible
        String getPriceScript = "return Array.from(document.querySelectorAll('span.atg_store_newPrice')).find(el => el.offsetParent !== null).textContent.trim();";

        String price = (String) jsExecutor.executeScript(getPriceScript);

        // Limpieza y conversión del precio
        price = price.replaceAll("[^0-9,]+", "").replace(",", "."); // Reemplazar coma por punto para él parseo
        Double precio = Double.parseDouble(price);

        HistorialPrecios historialPreciosAux = new HistorialPrecios();
        historialPreciosAux.setProducto(producto);
        historialPreciosAux.setPrecio(precio);
        historialPreciosAux.setFechaPrecio(LocalDate.now());

        return historialPreciosAux;
    }

}
