package com.lautaro;

import com.lautaro.service.scraping.CotoService;
import com.lautaro.service.scraping.DiaService;
import com.lautaro.service.scraping.JumboService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScrapingAppApplication {


    @Autowired
    DiaService diaService;
    @Autowired
    CotoService cotoService;
    @Autowired
    JumboService jumboService;

    public static void main(String[] args) {
      SpringApplication.run(ScrapingAppApplication.class, args);
    }

    @PostConstruct
    public void  init(){
        System.out.println("Iniciando servicio");
        jumboService.obtenerTodaLaInformacionJumbo();

    }
}





