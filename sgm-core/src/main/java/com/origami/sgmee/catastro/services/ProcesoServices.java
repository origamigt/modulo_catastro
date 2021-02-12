/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.services;

import com.origami.sgm.entities.CatEdfProp;
import com.origami.sgm.entities.CatEscritura;
import com.origami.sgm.entities.CatEscrituraPropietario;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioAlicuotaComponente;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioEdificacionProp;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.CatPredioS4;
import com.origami.sgm.entities.CatPredioS6;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.entities.FotoPredio;
import com.origami.sgm.entities.HistoricoTramiteDet;
import com.origami.sgm.entities.Observaciones;
import com.origami.sgmee.catastro.geotx.entity.GeoPrediosDivididos;
import com.origami.sgmee.catastro.util.CatPredioS6Uso;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

/**
 * provee metodos para las tareas e integracion con la base grafica.
 *
 * @author Dairon Freddy
 *
 */
public interface ProcesoServices {

    public CatPredio registrarPredio(CatPredio px);

    public String claveCatastral(CatPredio px);

    public BigInteger generarNumPredio();

    public CatPredioEdificacion registrarEdificacion(CatPredioEdificacion edificacion);

    public CatPredioEdificacion clonarEdificacion(CatPredioEdificacion edificacion, CatPredio predio, Short nroEdificacion, boolean save);

    public CatPredioEdificacionProp registrarEdificacionProp(CatPredioEdificacionProp detalle);

    public CatPredioEdificacionProp clonarEdificacionProp(CatPredioEdificacionProp detalle, CatPredioEdificacion edificacion, CatEdfProp prop, boolean save);

    public CatPredioS4 registrarPredioS4(CatPredioS4 predioS4);

    public CatPredioS4 clonarPredioS4(CatPredioS4 predioS4, CatPredio predio, boolean save, Long id, String... ignore);

    public CatPredioS4 clonarPredioS4(CatPredioS4 predioS4, CatPredio predio, Collection<CtlgItem> accesibilidad, boolean save, Long id, String... ignore);

    public CatPredioS6 registrarPredioS6(CatPredioS6 predioS6);

    public CatPredioS6 registrarPredioS6(CatPredioS6 predioS6, Collection<CtlgItem> vias, Collection<CtlgItem> instalaciones);

    public CatPredioS6 clonarPredioS6(CatPredioS6 predioS6, CatPredio predio, Collection<CtlgItem> vias, Collection<CtlgItem> instalaciones, boolean save, Long id, String... ignore);

    public CatPredioS6 clonarPredioS6(CatPredioS6 predioS6, CatPredio predio, Collection<CtlgItem> vias, Collection<CtlgItem> instalaciones, List<CatPredioS6Uso> usos, boolean save, Long id, String... ignore);

    public Collection<FotoPredio> cambiarPredio(CatPredio old, CatPredio predio);

    public void saveEdificacionesPredio(CatPredio predioRaiz, CatPredio predioResultante);

    public void saveFotosPredio(CatPredio predioRaiz, CatPredio predioResultante);

    public CatPredioPropietario clonarPropietario(CatPredioPropietario propietario, CatPredio predio);

    public CatPredio clonarPredio(CatPredio predio, Short codProv, String[] ingnore);

    public CatPredio asentarCambiosData(CatPredio actualizado, String[] propertyIgnores);

    public void prepararUsosPredio(CatPredio actualizado);

    public CatPredio asentarCambiosConstruccion(CatPredio predio, CatPredio actualizado);

    public CatPredioAlicuotaComponente registrarAlicuotaComponente(CatPredioAlicuotaComponente alicuota);

    public CatPredioAlicuotaComponente clonarAlicuotaComponente(CatPredioAlicuotaComponente alicuota, CatPredio predio, boolean save, Long id, String... ignore);

    public String generarClaveCatastral(CatPredio predio);

    public boolean eliminarPredioCompleto(CatPredio predio);

    /**
     * Realiza la Creqcion de los nuevos predios en el alfanumerico
     *
     * @param predio Predio matriz en proceso de division
     * @param geoPredios Predios Divididos
     * @return Lista de nuevas claves Creadas.
     */
    public List<CatPredio> dividirPredios(CatPredio predio, List<GeoPrediosDivididos> geoPredios);

    public CatPredio clonarPredioFusion(CatPredio predio, List<CatPredio> predios, Short codLote);

    public HistoricoTramiteDet updateDetalle(HistoricoTramiteDet detalle);

    public CatEscritura clonarEscrituras(CatEscritura escritura, CatPredio predio, List<CatEscrituraPropietario> escrituraPropietarioList, Long id, String... ignore);

    public CatEscritura registrarEscritura(CatEscritura escritura);

    public Observaciones guardarObservaciones(Long idTramite, String nameUser, String observaciones, String taskName, Long idPredio);
}
