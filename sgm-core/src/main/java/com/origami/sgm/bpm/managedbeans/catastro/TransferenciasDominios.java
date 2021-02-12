/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro;

import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.entities.CatEscrituraPropietario;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import util.JsfUti;

/**
 * Controlador del facelet de transferecniasDominios, donde se visualiza las que
 * ha sido ingresadas y las acentadas
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class TransferenciasDominios implements Serializable {

    private static final Logger LOG = Logger.getLogger(TransferenciasDominios.class.getName());

    protected CatEscrituraPropietario cep;
    protected BaseLazyDataModel<CatEscrituraPropietario> lazy;

    @Inject
    protected ServletSession ss;
    @Inject
    protected UserSession sess;
    @Inject
    protected Entitymanager manager;

    /**
     * Inicializa los datos del formulario
     */
    @PostConstruct
    public void load() {
        try {
            lazy = new BaseLazyDataModel<>(CatEscrituraPropietario.class, "id", "DESC");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void acentarTransferecnia(CatEscrituraPropietario trans) {
        if (trans.getEscritura() != null) {
            if (trans.getEscritura().getEstado().equalsIgnoreCase(EstadosPredio.TEMPORAL)
                    || trans.getPropietario().getEstado().equalsIgnoreCase(EstadosPredio.TEMPORAL)) {
                ss.agregarParametro("numPredio", trans.getPropietario().getPredio().getNumPredio());
                ss.agregarParametro("idPredio", trans.getPropietario().getPredio().getId());
                ss.agregarParametro("idTranferecnia", trans.getId());
                ss.agregarParametro("imprimir", false);
                JsfUti.redirectFaces("/faces/vistaprocesos/catastro/transferenciaDominio/transferenciaDominio.xhtml");
            } else {
                JsfUti.messageError(null, "Error", "Registro no esta en estado temporal.");
            }
        } else {
            JsfUti.messageError(null, "Error", "Registro no esta en estado temporal.");
        }
    }

    /**
     * Reimprime la actualizacion catastral
     *
     * @param trans CatEscrituraPropietario a reimprimir
     */
    public void imprimir(CatEscrituraPropietario trans) {
        if (trans == null) {
            JsfUti.messageError(null, "Error", "Registro no esta en estado temporal.");
            return;
        }
        ss.instanciarParametros();
        ss.setNombreDocumento("actualizacionCatastral" + trans.getEscritura().getNumeroActualizacion());
        ss.setNombreSubCarpeta("/catastro/certificados/");
        ss.setNombreReporte("certificadoActualizacionCatastral");
        ss.agregarParametro("LOGO", SisVars.logoReportes);
        ss.agregarParametro("LOGO_1", SisVars.sisLogo1);
        String footer = SisVars.sisLogo1.substring(0, SisVars.sisLogo1.length() - 6) + "-footer.png";
        ss.agregarParametro("LOGO_FOOTER", footer);
        ss.agregarParametro("TITULO", SisVars.NOMBREMUNICIPIO);
        ss.agregarParametro("SUBREPORT_DIR", SisVars.REPORTES + "/");
        ss.agregarParametro("NOMBRE_CERTIFICADO", "ACTUALIZACION CATASTRAL");
        ss.agregarParametro("FUNCIONARIO", sess.getNombrePersonaLogeada());
        ss.setTieneDatasource(Boolean.FALSE);
        ss.setDataSource(Arrays.asList(trans));
        JsfUti.redirectFacesNewTab("/Documento");
    }

    public void eliminar(CatEscrituraPropietario trans) {
        this.manager.delete(trans);
        this.manager.delete(trans.getEscritura());
        this.manager.delete(trans.getPropietario());
    }

    /**
     * Variable para almacenar el CatEscrituraPropietario seleccionado
     *
     * @return
     */
    public CatEscrituraPropietario getCep() {
        return cep;
    }

    /**
     * Metodo de asignacion de CatEscrituraPropietario seleccionado
     *
     * @param cep
     */
    public void setCep(CatEscrituraPropietario cep) {
        this.cep = cep;
    }

    /**
     * Lazy de CatEscrituraPropietario
     *
     * @return listado paginado
     */
    public BaseLazyDataModel<CatEscrituraPropietario> getLazy() {
        return lazy;
    }

    /**
     * Metodo de asignacion de Lazy de CatEscrituraPropietario
     *
     * @param lazy
     */
    public void setLazy(BaseLazyDataModel<CatEscrituraPropietario> lazy) {
        this.lazy = lazy;
    }

}
