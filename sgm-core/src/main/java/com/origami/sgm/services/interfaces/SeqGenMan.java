/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.interfaces;

import com.origami.sgm.entities.CatEnte;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.PePermiso;
import com.origami.sgm.entities.UserConTareas;
import java.math.BigInteger;
import javax.ejb.Local;

/**
 * Interfaz con los metodos que generan las secuencias.
 *
 * @author CarlosLoorVargas
 */
@Local
public interface SeqGenMan {

    public BigInteger getSequence(String query, String[] params, Object[] values);

    public Object getSequences(String query, String[] params, Object[] values);

    public UserConTareas getUserConMenosTareas(Long rol, Integer cantidad);

    public Long getMaxNumeroFichaByTipo(Long tipoFicha);

    /**
     * Genera la secuencia de los numero de segimiento del
     * {@link HistoricoTramites}
     *
     * @param app Para la aplicacion actual se envia 'SGM'
     * @return numero de segimiento(columna de {@link HistoricoTramites}).
     */
    public Long getSecuenciasTram(String app);

    public PePermiso getSequences(PePermiso permisoNuevo);

    /**
     * Consulta el ultimo Número de predio generado en CatPredio y lo incrementa
     * en uno, se asigna el número de predio al campo numPredio y envia a
     * persistir la entiti para retornar la misma entiti persistida.
     *
     * @param predio Entiti CatPredio
     * @return CatPredio
     */
    public CatPredio generarNumPredioAndGuardarCatPredio(CatPredio predio);

    public Long getMaxSecuenciaTipoTramite(Integer anio, Long idTipoTramite);

    public CatEnte guardarOActualizarEnte(CatEnte ente);

    public BigInteger getMaxSecuenciaTipoLiquidacion(Integer anio, Long idTipoLiquidacion);

    public Long getNumComprobante();

}
