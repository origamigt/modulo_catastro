/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.interfaces.censocat;

import com.origami.sgm.entities.CatPredio;
import javax.ejb.Local;

/**
 * Interfaz con los metodos de procesamiento de los datos del predio
 *
 * @author CarlosLoorVargas
 */
@Local
public interface CensoServices {

    public CatPredio procesarPredio(CatPredio predioCensado, CatPredio Act);

}
