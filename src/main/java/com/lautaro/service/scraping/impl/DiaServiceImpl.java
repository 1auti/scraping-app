    package com.lautaro.service.scraping.impl;
    import com.cloudinary.utils.*;
    import com.cloudinary.Cloudinary;
    import com.cloudinary.utils.ObjectUtils;
    import com.lautaro.entitiy.*;
    import com.lautaro.service.ImageScraper;
    import com.lautaro.service.IncrementalSaveSystem;
    import com.lautaro.service.RetryUtil;
    import com.lautaro.service.scraping.DiaService;
    import io.github.cdimascio.dotenv.Dotenv;
    import lombok.RequiredArgsConstructor;
    import org.openqa.selenium.*;
    import org.openqa.selenium.NoSuchElementException;
    import org.openqa.selenium.interactions.Actions;
    import org.openqa.selenium.support.ui.ExpectedConditions;
    import org.openqa.selenium.support.ui.WebDriverWait;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Service;
    import java.io.IOException;
    import java.net.URLEncoder;
    import java.time.Duration;
    import java.time.LocalDate;
    import java.util.*;

    import static com.lautaro.service.WebDriverConfig.configurarWebDriver;


    @Service
    @RequiredArgsConstructor
    public class DiaServiceImpl implements DiaService {

        private static final String COTO_BASE_URL = "https://diaonline.supermercadosdia.com.ar/";
        private static final Logger log = LoggerFactory.getLogger(DiaServiceImpl.class);
        private final IncrementalSaveSystem incrementalSaveSystem;




        public Supermercado obtenerTodaLaInformacionDia() {

                WebDriver driver = null;
                Supermercado supermercado = new Supermercado();
                supermercado.setNombre("Dia");
                supermercado = incrementalSaveSystem.saveSupermercado(supermercado);

                try {
                    driver = configurarWebDriver();
                    obtenerCategorias(supermercado);
                    supermercado = incrementalSaveSystem.saveSupermercado(supermercado);
                    log.info("Información de Dia obtenida con éxito");
                    return supermercado;
                } catch (Exception e) {
                    log.error("Error al obtener información de Dia", e);
                    throw new RuntimeException("Error al obtener información de Dia", e);
                } finally {
                    if (driver != null) {
                        driver.quit();
                    }
                }

        }
    /*
        @Override
        public void actualizarPreciosDIa() {
            List<Producto> productos = supermercadoService.obtenerProductosPorSupermercado("Dia");

            for (Producto producto : productos) {
                try {
                    WebDriver driver = configurarWebDriver();
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                    driver.get(producto.getLink());
                    HistorialPrecios historialPreciosActual = obtenerPrecio(wait, producto);
                    if (!Objects.equals(historialPreciosActual.getPrecio(), producto.getHistorialPrecios().get(producto.getHistorialPrecios().size() - 1).getPrecio())) {
                        producto.getHistorialPrecios().add(historialPreciosActual);
                    }
                    driver.quit();
                } catch (Exception e) {
                    // Manejo de la excepción
                    System.err.println("Error al actualizar el precio del producto: " + e.getMessage());
                }
            }
        }

     */


        private void obtenerCategorias(Supermercado supermercado) {
            if (supermercado.getId() == null) {
                throw new IllegalStateException("El supermercado debe tener un ID antes de procesar las categorías");
            }
            WebDriver driver = configurarWebDriver();
            List<Categoria> categorias = new ArrayList<>();
            try {
                driver.get(COTO_BASE_URL);
                Thread.sleep(2000);

                WebElement categoriasButton = driver.findElement(By.cssSelector("div.diaio-custom-mega-menu-0-x-custom-mega-menu-trigger__button"));
                categoriasButton.click();

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.diaio-custom-mega-menu-0-x-category-list__container")));

                List<WebElement> categoryLinks = driver.findElements(By.cssSelector("div.diaio-custom-mega-menu-0-x-category-list__container a"));

                for (WebElement categoryLink : categoryLinks) {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(categoryLink.getText());
                    categoria.setLink(categoryLink.getAttribute("href"));
                    categoria.setSupermercado(supermercado);
                    log.info("Categoria: {}", categoria.getNombre());
                    categoria = incrementalSaveSystem.saveCategoria(categoria);
                    List<Subcategoria> subcategorias = obtenerSubcategorias(driver, categoryLink, categoria, supermercado);
                    categoria.setSubcategorias(subcategorias);
                    categoria = incrementalSaveSystem.saveCategoria(categoria);
                    supermercado.getCategorias().add(categoria);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                driver.quit();
            }
        }


//        private List<Categoria> obtenerCategorias(Supermercado supermercado) {
//            if (supermercado.getId() == null) {
//                throw new IllegalStateException("El supermercado debe tener un ID antes de procesar las categorías");
//            }
//
//            List<String> categoryUrls = new ArrayList<>();
//
//            // Usar un WebDriver inicial para obtener los enlaces de las categorías
//            try{
//                WebDriver initialDriver = configurarWebDriver();
//                initialDriver.get(COTO_BASE_URL);
//
//                new WebDriverWait(initialDriver, Duration.ofSeconds(10))
//                        .until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.diaio-custom-mega-menu-0-x-custom-mega-menu-trigger__button")))
//                        .click();
//
//                new WebDriverWait(initialDriver, Duration.ofSeconds(10))
//                        .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.diaio-custom-mega-menu-0-x-category-list__container")));
//
//                List<WebElement> categoryLinks = initialDriver.findElements(By.cssSelector("div.diaio-custom-mega-menu-0-x-category-list__container a"));
//
//                categoryUrls = categoryLinks.stream()
//                        .map(link -> link.getAttribute("href"))
//                        .collect(Collectors.toList());
//            }catch (Exception e){
//                log.info("No se ha podido obtener la informacion de la pagina {}",e.getMessage());
//            }
//
//            // Usar un ExecutorService para procesar las categorías en paralelo
//            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//
//            try {
//                List<Future<Categoria>> futures = categoryUrls.stream()
//                        .map(url -> executor.submit(() -> procesarCategoria(url, supermercado)))
//                        .collect(Collectors.toList());
//
//                // Esperar y recoger los resultados
//                return futures.stream()
//                        .map(future -> {
//                            try {
//                                return future.get();
//                            } catch (Exception e) {
//                                log.error("Error al procesar categoría: ", e);
//                                return null;
//                            }
//                        })
//                        .filter(Objects::nonNull)
//                        .collect(Collectors.toList());
//            } finally {
//                executor.shutdown();
//            }
//        }
//
//        private Categoria procesarCategoria(String url, Supermercado supermercado) {
//            try{
//                WebDriver driver = configurarWebDriver();
//                driver.get(url);
//
//                WebElement categoryElement = driver.findElement(By.cssSelector("selector-para-nombre-categoria"));
//                String nombreCategoria = categoryElement.getText();
//
//                Categoria categoria = new Categoria();
//                categoria.setNombre(nombreCategoria);
//                categoria.setLink(url);
//                categoria.setSupermercado(supermercado);
//
//                log.info("Procesando Categoria: {}", categoria.getNombre());
//
//                categoria = incrementalSaveSystem.saveCategoria(categoria);
//                List<Subcategoria> subcategorias = obtenerSubcategorias(driver, categoria, supermercado);
//                categoria.setSubcategorias(subcategorias);
//
//                return incrementalSaveSystem.saveCategoria(categoria);
//            } catch (Exception e) {
//                log.error("Error al procesar categoría {}: ", url, e);
//                throw new RuntimeException("Error al procesar categoría", e);
//            }
//        }

        private List<Subcategoria> obtenerSubcategorias(WebDriver driver,WebElement categoryLink, Categoria categoria, Supermercado supermercado) {
            log.info("Iniciando proceso de obtener subcategorias: {} ", categoria.getNombre());
        Actions actions = new Actions(driver);
        actions.moveToElement(categoryLink).perform();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> subcategoryLinks = driver.findElements(By.xpath("//a[contains(@class, 'diaio-custom-mega-menu-0-x-category-list-item__container')]"));
        List<Subcategoria> subcategorias = new ArrayList<>();

        String[] palabrasExcluir = {"Almacén", "Bebidas", "Frescos","Desayuno","Limpieza","Perfumería","Congelados","Bebés y Niños","Hogar y Deco","Mascotas","Frutas y Verduras","Electro Hogar","Kiosco"};
        String regex = String.join("|", palabrasExcluir);

        for (WebElement subcategoryLink : subcategoryLinks) {
            String subcategoriaNombre = subcategoryLink.getText();
            if (!subcategoriaNombre.matches(".*(" + regex + ").*") ) {
                Subcategoria subcategoria = new Subcategoria();
                subcategoria.setNombre(subcategoryLink.getText());
                subcategoria.setCategoria(categoria);
                subcategoria.setLink(subcategoryLink.getAttribute("href"));
                log.info("Nombre: {}  Link : {}",subcategoria.getNombre(),subcategoria.getLink());
                subcategoria = incrementalSaveSystem.saveSubcategoria(subcategoria);
                // Comprueba si la subcategoría se guardó correctamente
                if (subcategoria.getId() != null) {
                    List<Tipo> tipoList = obtenerTipos(driver,subcategoryLink,subcategoria,supermercado,categoria);
                    subcategoria.setTipoList(tipoList);
                    subcategoria = incrementalSaveSystem.saveSubcategoria(subcategoria);
                    subcategorias.add(subcategoria);
                } else {
                    log.error("Error al guardar la subcategoría: {}", subcategoria.getNombre());
                }
            }
        }

        return subcategorias;
        }

        private List<Tipo> obtenerTipos(WebDriver driver, WebElement subcategoryLink, Subcategoria subcategoria, Supermercado supermercado, Categoria categoria) {

                log.info("Obteniendo tipos para la subcategoría: {}", subcategoria.getNombre());
                Actions actions = new Actions(driver);
                actions.moveToElement(subcategoryLink).perform();
            try {
            Thread.sleep(1000);
       } catch (InterruptedException e) {
          log.info("Ha ocurrido un error en el servicio tipos");
       }

                List<WebElement> typeLinks = driver.findElements(By.cssSelector("div.diaio-custom-mega-menu-0-x-category-list__container a"));
                List<Tipo> tipos = new ArrayList<>();

                String[] palabrasExcluir = {
                        "Almacén", "Bebidas", "Frescos", "Desayuno", "Limpieza", "Perfumería", "Congelados", "Bebés y Niños", "Hogar y Deco",
                        "Mascotas", "Frutas y Verduras", "Electro Hogar", "Kiosco", "Conservas", "Aceite y aderezos", "Pastas secas", "Arroz y legumbres",
                        "Panaderia", "Golosinas y alfajores", "Repostería", "Comidas listas", "Harinas", "Picadas", "Pan rallado y rebozadores",
                        "Gaseosas", "Cervezas", "Aguas", "Bodega", "Jugos e isotonicas", "Leches", "Fiambrería", "Lácteos", "Carnicería",
                        "Pastas frescas", "Listos para disfrutar", "Galletitas y cereales", "Infusiones y endulzantes", "Para untar", "Cuidado de la ropa", "Papelería",
                        "Limpiadores", "Limpieza de cocina", "Accesorios de limpieza", "Desodorantes de ambiente", "Insecticidas", "Fósforos y velas",
                        "Bolsas", "Cuidado del pelo", "Cuidado personal", "Cuidado bucal", "Jabones", "Protección femenina", "Máquinas de afeitar",
                        "Farmacia", "Hamburguesas y medallones", "Rebozados", "Vegetales congelados", "Postres congelados", "Pescadería",
                        "Papas congeladas", "Comidas congeladas", "Pañales", "Cuidado del bebé", "Alimentación para bebés", "Juegos y juguetes", "Librería",
                        "Deco hogar", "Ferretería", "Cocina", "Gatos", "Perros", "Frutas", "Verduras", "Huevos", "Frutos secos",
                        "Panadería", "Aceites y aderezos"
                };
                Set<String> palabrasExcluirSet = new HashSet<>(Arrays.asList(palabrasExcluir));

            for (WebElement typeLink : typeLinks) {
            String typeName = typeLink.getText();
            boolean ignore = false;
            for (String palabra : palabrasExcluir) {
                if (typeName.equals(palabra)) {
                    ignore = true;
                    break;
                }
            }
            if (!typeName.equals("Ver todos") && !ignore) {
                Tipo tipo = new Tipo();
                tipo.setNombre(typeName);
                tipo.setLink(typeLink.getAttribute("href"));
                tipo.setSubcategoria(subcategoria);
                log.info("Nombre {} Link {}",tipo.getNombre(), tipo.getLink());
                tipo = incrementalSaveSystem.saveTipo(tipo);
                List<Producto> productos = obtenerProductos(tipo.getLink(), tipo , supermercado,categoria ,subcategoria);
                tipo.setProductoList(productos);
                tipo = incrementalSaveSystem.saveTipo(tipo);
                tipos.add(tipo);
            }
        }
        return tipos;
        }

        private List<Producto> obtenerProductos(String tipoUrl, Tipo tipo, Supermercado supermercado, Categoria categoria, Subcategoria subcategoria) {
            log.info("Obteniendo productos");
        WebDriver driver = configurarWebDriver();
        try {
            driver.get(tipoUrl);
            Thread.sleep(2000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

            while (true) {
                js.executeScript("window.scrollBy(0, 1500);");
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long newHeight = (long) js.executeScript("return document.body.scrollHeight");
                if (newHeight == lastHeight) {
                    break;
                }
                lastHeight = newHeight;
            }

            List<WebElement> productLinks = driver.findElements(By.cssSelector("a.vtex-product-summary-2-x-clearLink"));
            List<Producto> productos = new ArrayList<>();

            for (WebElement productElement : productLinks) {
                Producto producto = new Producto();
                producto.setLink(productElement.getAttribute("href"));
                producto.setTipo(tipo);
                producto.setSupermercado(supermercado);
                producto = incrementalSaveSystem.saveProducto(producto);
                producto = obtenerProducto(producto,supermercado, categoria , subcategoria, tipo);
                supermercado.getProductos().add(producto);
                supermercado = incrementalSaveSystem.saveSupermercado(supermercado);
                producto = incrementalSaveSystem.saveProducto(producto);
                productos.add(producto);
            }

            return productos;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        }

        private Producto obtenerProducto(Producto producto, Supermercado supermercado, Categoria categoria, Subcategoria subcategoria, Tipo tipo) {
            return RetryUtil.retry(() -> {
                log.info("Obteniendo detalles del producto: {}", producto.getLink());
                WebDriver driver = configurarWebDriver();
                try {
                    driver.get(producto.getLink());
                    Thread.sleep(2000);
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

                    Dotenv dotenv = Dotenv.load();
                    Cloudinary cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
                    cloudinary.config.secure = true;

                    String nombreCategoria = categoria.getNombre();
                    String nombreSubcategoria = subcategoria.getNombre();
                    String nombreTipo = tipo.getNombre();

                    producto.getHistorialPrecios().add(obtenerPrecio(wait, producto));

                    WebElement brandElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector(".vtex-store-components-3-x-productBrandName")));
                    producto.setMarca(brandElement.getText());

                    WebElement nameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector(".vtex-store-components-3-x-productNameContainer")));
                    producto.setNombre(nameElement.getText());


                    ImageScraper scraper = new ImageScraper();
                    List<String> imageUrls = scraper.scrapeImages(driver);

                    for (int i = 0; i < imageUrls.size(); i++) {
                        String imageUrl = imageUrls.get(i);
                        try {
                            String nombreImagen = generarNombreImagen(producto) + "_" + (i + 1);
                            String folder = sanitizeFolderName("supermercados/Dia/" + categoria.getNombre() + "/" + subcategoria.getNombre() + "/" + tipo.getNombre() + "/" + producto.getNombre());
                            Imagen imagen = uploadImage(cloudinary, imageUrl, producto, folder, nombreImagen);
                            producto.getImagenes().add(imagen);
                        } catch (IOException e) {
                            log.error("Error al subir la imagen del producto: " + imageUrl, e);
                        }
                    }


                    try {
                        // Intentar obtener la descripción desde el contenedor específico
                        WebElement descriptionElement = driver.findElement(By.cssSelector("div.vtex-store-components-3-x-productDescriptionText > div > div[style='display:contents']"));
                        producto.setDescripcion(descriptionElement.getText());
                    } catch (NoSuchElementException e) {
                        producto.setDescripcion("No disponible");
                    }

                    producto.setFechaIngreso(LocalDate.now());

                    //Ingredientes y Valor nutricional no estan disponibles
                    producto.setIngredientes("No disponible");

                    log.info("Producto obtenido con éxito: {}", producto.getNombre());
                    return producto;
                } finally {
                    driver.quit();
                }
            }, 3, 1000);
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

        private String generarNombreImagen(Producto producto) {
            return sanitizeString(producto.getNombre());
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
                log.error("Error al cargar la imagen: " + e.getMessage(), e);
                throw new RuntimeException("Error al cargar la imagen", e);
            }
        }






        private HistorialPrecios obtenerPrecio(WebDriverWait wait , Producto producto){

            WebElement priceElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".vtex-product-price-1-x-sellingPrice")));
            String price = priceElement.getText();
            String precioAux = price.replaceAll("[^0-9,\\.]+", "").replace(',', '.');
            precioAux = precioAux.replaceAll("\\.(?=.*\\.)", ""); // Eliminar puntos adicionales
            Double precio = Double.parseDouble(precioAux);

            HistorialPrecios historialPrecios = new HistorialPrecios();
            historialPrecios.setPrecio(precio);
            historialPrecios.setFechaPrecio(LocalDate.now());
            historialPrecios.setProducto(producto);

            return historialPrecios;
        }


    }