package com.lautaro.service;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.commons.io.IOUtils;

import java.io.*;

@Data
@AllArgsConstructor
public class ImagenDownloader {
    private String baseCarpetaDestino;

    public void descargarImagen(String imgUrl, String nombreProducto, String nombreArchivo) throws IOException {
        String carpetaProducto = baseCarpetaDestino + File.separator + nombreProducto;
        File directorio = new File(carpetaProducto);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        CloseableHttpClient httpClient = HttpClients.custom()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build();
        HttpGet request = new HttpGet(imgUrl);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            //System.out.println("Código de respuesta para " + nombreArchivo + ": " + response.getStatusLine().getStatusCode());

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String contentType = entity.getContentType().getValue();
                //System.out.println("Tipo de contenido para " + nombreArchivo + ": " + contentType);

                String extension = getExtensionFromContentType(contentType);
                String rutaArchivo = carpetaProducto + File.separator + nombreArchivo + "." + extension;

                // Descargar la imagen
                try (InputStream inputStream = entity.getContent();
                     FileOutputStream outputStream = new FileOutputStream(rutaArchivo)) {
                    IOUtils.copy(inputStream, outputStream);
                }

                //System.out.println("Imagen guardada en: " + rutaArchivo);
                //System.out.println("Tamaño del archivo: " + new File(rutaArchivo).length() + " bytes");

                // Convertir SVG a JPG si es necesario
                if (extension.equals("svg")) {
                    String rutaJpg = carpetaProducto + File.separator + nombreArchivo + ".jpg";
                    convertirSvgAJpg(rutaArchivo, rutaJpg);
                }

            } else {
                System.out.println("No se pudo obtener el contenido de la imagen: " + imgUrl);
            }
        } catch (IOException e) {
            System.err.println("Error al descargar la imagen: " + imgUrl);
            e.printStackTrace();
        } finally {
            httpClient.close();
        }
    }

    private String getExtensionFromContentType(String contentType) {
        if (contentType.contains("png")) return "png";
        if (contentType.contains("gif")) return "gif";
        if (contentType.contains("jpeg") || contentType.contains("jpg")) return "jpg";
        if (contentType.contains("webp")) return "webp";
        if (contentType.contains("svg")) return "svg";
        return "bin"; // archivo binario genérico si no se reconoce el tipo
    }

    private void convertirSvgAJpg(String svgFilePath, String jpgFilePath) {
        try {
            // Crear un InputStream para el archivo SVG
            FileInputStream svgInputStream = new FileInputStream(svgFilePath);

            // Crear un TranscoderInput para el SVG
            TranscoderInput input = new TranscoderInput(svgInputStream);

            // Crear un OutputStream para el archivo JPG
            OutputStream jpgOutputStream = new FileOutputStream(jpgFilePath);

            // Crear un TranscoderOutput para el JPG
            TranscoderOutput output = new TranscoderOutput(jpgOutputStream);

            // Crear una instancia de JPEGTranscoder
            JPEGTranscoder transcoder = new JPEGTranscoder();

            // Configurar las propiedades de salida del JPG (calidad, etc.)
            transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 0.8f); // Calidad 80%

            // Realizar la conversión
            transcoder.transcode(input, output);

            // Cerrar los flujos
            jpgOutputStream.close();
            svgInputStream.close();

            //System.out.println("SVG convertido a JPG exitosamente en: " + jpgFilePath);
        } catch (Exception e) {
            //System.err.println("Error al convertir SVG a JPG: " + svgFilePath);
            e.printStackTrace();
        }
    }
}