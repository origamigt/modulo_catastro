/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.censocat.service.ordenes;

import com.origami.censocat.entity.ordentrabajo.OrdenDet;
import com.origami.censocat.entity.ordentrabajo.OrdenTrabajo;
import com.origami.censocat.models.FotosModel;
import com.origami.censocat.models.ResumenOrdenes;
import com.origami.sgm.entities.CatPredio;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author CarlosLoorVargas
 */
@Local
public interface OrdenService {

    public OrdenTrabajo guardarOrden(OrdenTrabajo ot, List<OrdenDet> dt);

    public OrdenDet guardarDetOrden(OrdenDet d);

    public List<ResumenOrdenes> getResumenGeneral(String query, int tipo);

    public OrdenDet getOrdenDet(Long idOrdenDet);

    /**
     * Si el id de la orden es nulo envia persister la orden, caso contrario
     * solo realiza el update
     *
     * @param orden
     * @return
     */
    public OrdenTrabajo guardarOrden(OrdenTrabajo orden);

    public Boolean procesarPredio(CatPredio predioCenso, CatPredio predioAct, List<FotosModel> fotosModel);

}
