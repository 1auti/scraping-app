package com.lautaro.service.scraping;

import com.lautaro.entitiy.Producto;
import com.lautaro.entitiy.Supermercado;
import lombok.NoArgsConstructor;

import java.util.List;


public interface JumboService {

    Supermercado obtenerTodaLaInformacionJumbo();
    //void actualizarPreciosJumbo();
}
