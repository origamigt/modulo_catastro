/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.cdi.valora;

import com.origami.catastroextras.entities.ibarra.EjeComercial;
import com.origami.catastroextras.entities.ibarra.EjeComercialPredio;
import com.origami.catastroextras.entities.ibarra.ValoraAfectacion;
import com.origami.catastroextras.entities.ibarra.ValoraMnz;
import com.origami.config.SisVars;
import com.origami.session.UserSession;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.events.ValorarPredioPost;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.hibernate.HibernateException;
import util.JsfUti;
import util.Utils;

/**
 * Mantenimiento para las tablas de valoracion que son
 * {@link ValoraMnz}, {@link ValoraAfectacion}, {@link EjeComercialPredio}, {@link EjeComercial}
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class ValoraClavesView implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8711448364936567629L;

    private static final Logger LOG = Logger.getLogger(ValoraClavesView.class.getName());

    @Inject
    private Entitymanager manager;
    @Inject
    private CatastroServices catastroServices;
    @Inject
    private UserSession userSession;

    private Boolean ver = true;
    private BaseLazyDataModel<ValoraMnz> valoraMzLazy;
    private ValoraMnz valoraMzTemp;

    protected BaseLazyDataModel<EjeComercialPredio> ejeComercialPrediosLazy;
    protected EjeComercialPredio ejeComercialPredioTemp;

    protected BaseLazyDataModel<ValoraAfectacion> valoraAfectacionsLazy;
    protected ValoraAfectacion valoraAfectacionTemp;

    protected BaseLazyDataModel<EjeComercial> ejeComercialLazy;
    protected EjeComercial ejeComercialTemp;

    protected List<Integer> estados = Arrays.asList(0, 1);
    protected Boolean agregoObservacion = false;
    protected String observacion;
    protected List<String> observaciones;
    @Inject
    private Event<ValorarPredioPost> valoracion;

    /**
     * Inicializa los Lazy para cada uno de los data table
     */
    @PostConstruct
    public void load() {
        try {
            if (!JsfUti.isAjaxRequest()) {
                valoraMzLazy = new BaseLazyDataModel<>(ValoraMnz.class, "claveTotal");
                valoraAfectacionsLazy = new BaseLazyDataModel<>(ValoraAfectacion.class, "codCatastralPredio");
                ejeComercialPrediosLazy = new BaseLazyDataModel<>(EjeComercialPredio.class, "codigoEjeComercial");
                ejeComercialLazy = new BaseLazyDataModel<>(EjeComercial.class, "descripcion");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Init View", e);
        }
    }

    /**
     * habre el dialog framework para la creacion, edicion, o vista del catalogo
     * restriccion
     *
     * @param cd Restricciones
     */
    public void nuevoMz(ValoraMnz cd) {
        if (cd == null) {
            valoraMzTemp = new ValoraMnz();
        } else {
            valoraMzTemp = cd;
        }
        ver = false;
        if (valoraMzTemp.getObservaciones() != null) {
            observaciones = Arrays.asList(valoraMzTemp.getObservaciones().split("\n"));
        } else {
            observaciones = null;
        }
        JsfUti.executeJS("PF('dglEdicionManzana').show()");
    }

    /**
     * Abre el dialogo para ingresar un predio en un eje comercial
     *
     * @param cd
     */
    public void nuevoEje(EjeComercialPredio cd) {
        if (cd == null) {
            ejeComercialPredioTemp = new EjeComercialPredio();
        } else {
            ejeComercialPredioTemp = cd;
        }
        ver = false;
        if (ejeComercialPredioTemp.getObservaciones() != null) {
            observaciones = Arrays.asList(ejeComercialPredioTemp.getObservaciones().split("\n"));
        } else {
            observaciones = null;
        }
        JsfUti.executeJS("PF('dglEditarEjeComerciales').show()");
    }

    /**
     * habre el dialogo para ingresar o actualizar los datos de un predios que
     * tiene afectacion
     *
     * @param cd
     */
    public void nuevoAfect(ValoraAfectacion cd) {
        if (cd == null) {
            valoraAfectacionTemp = new ValoraAfectacion();
        } else {
            valoraAfectacionTemp = cd;
        }
        ver = false;
        if (valoraAfectacionTemp.getObservaciones() != null) {
            observaciones = Arrays.asList(valoraAfectacionTemp.getObservaciones().split("\n"));
        } else {
            observaciones = null;
        }
        JsfUti.executeJS("PF('dglEditarAfectaciones').show()");
    }

    /**
     * Abre el dialogo para ingresar o actualizar los datos de ejes comerciales.
     *
     * @param cd
     */
    public void nuevoEjeComerc(EjeComercial cd) {
        if (cd == null) {
            ejeComercialTemp = new EjeComercial();
        } else {
            ejeComercialTemp = cd;
        }
        ver = false;
        if (ejeComercialTemp.getObservaciones() != null) {
            observaciones = Arrays.asList(ejeComercialTemp.getObservaciones().split("\n"));
        } else {
            observaciones = null;
        }
        JsfUti.executeJS("PF('dglCatalogoEjeComercial').show()");
    }

    /**
     * Envia a guardar o modificar el registro que se esta editando en el
     * momento.
     *
     */
    public void guardar() {
        if (!this.agregoObservacion) {
            JsfUti.messageInfo(null, "Error", "Debe ingresar la observacion");
            return;
        }
        if (valoraMzTemp == null) {
            JsfUti.messageError(null, "Error",
                    "Ocurrio un error en el registro seleccionado, cierre el dialogo y vuelva abrirlo.");
            return;
        }
        if (valoraMzTemp.getParroquia() == null) {
            JsfUti.messageError(null, "Advertencia", "Debe ingresar la clave catastral.");
            return;
        }
        if (valoraMzTemp.getZona() == null) {
            JsfUti.messageError(null, "Advertencia", "Debe ingresar la clave catastral.");
            return;
        }
        if (valoraMzTemp.getSector() == null) {
            JsfUti.messageError(null, "Advertencia", "Debe ingresar la clave catastral.");
            return;
        }
        if (valoraMzTemp.getManzana() == null) {
            JsfUti.messageError(null, "Advertencia", "Debe ingresar la clave catastral.");
            return;
        }
        if (valoraMzTemp.getClaveTotal() == null) {
            JsfUti.messageError(null, "Advertencia", "Debe ingresar la clave catastral.");
            return;
        }
        try {
            manager.persist(valoraMzTemp);
        } catch (HibernateException e) {
            JsfUti.messageFatal(null, "Error en transaccion", e.getMessage());
            return;
        }
        // FacesContext.getCurrentInstance().responseComplete();
        JsfUti.update("frmValoraClaves:container:dtValoraManzana");
        JsfUti.executeJS("PF('dglEdicionManzana').hide()");
        JsfUti.messageInfo(null, "Informacion", "Registros almacenados correctamente");

        ver = false;
    }

    /**
     * Guarda la ediccion o insercion de un eje comercial.
     */
    public void guardarEjeComercial() {
        try {
            if (!this.agregoObservacion) {
                JsfUti.messageInfo(null, "Error", "Debe ingresar la observacion");
                return;
            }
            if (ejeComercialPredioTemp == null) {
                JsfUti.messageError(null, "Error",
                        "Ocurrio un error en el registro seleccionado, cierre el dialogo y vuelva abrirlo.");
                return;
            }

            if (ejeComercialPredioTemp.getCodCatastralPredio() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar la clave catastral.");
                return;
            }
            if (ejeComercialPredioTemp.getCodigoEjeComercial() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar el eje comercial.");
                return;
            }
            if (ejeComercialPredioTemp.getAfectacionPredio() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar el valor de Afectacion.");
                return;
            }
            manager.persist(ejeComercialPredioTemp);
            CatPredio predio = catastroServices.getPredioByClaveCatAnt(ejeComercialPredioTemp.getCodCatastralPredio(), "A");
            // LANZAMOS EL EVENTO DE VALORACION DEL PREDIO
            this.valorarPredio(predio);
            JsfUti.update("frmValoraClaves");
            JsfUti.messageInfo(null, "Informacion", "Registros almacenados correctamente");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error el guardar", e);
            JsfUti.messageFatal(null, "Error en transaccion", e.getMessage());
        }
        ver = true;
    }

    private void valorarPredio(CatPredio predio) {
        if (predio != null) {
            valoracion.fire(new ValorarPredioPost(predio.getClaveCat(), predio.getPredialant(), 1, predio.getTipoPredio()));
        }
    }

    /**
     * Guarda la edicion o modificacion de un valor de afectacion
     */
    public void guardarValoraAfetacion() {
        try {
            if (!this.agregoObservacion) {
                JsfUti.messageInfo(null, "Error", "Debe ingresar la observacion");
                return;
            }
            if (valoraAfectacionTemp == null) {
                JsfUti.messageError(null, "Error",
                        "Ocurrio un error en el registro seleccionado, cierre el dialogo y vuelva abrirlo.");
                return;
            }

            if (valoraAfectacionTemp.getCodCatastralPredio() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar la clave catastral.");
                return;
            }
            if (valoraAfectacionTemp.getParroquia() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar Parroquia.");
                return;
            }
            if (valoraAfectacionTemp.getZona() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar la Zona.");
                return;
            }
            if (valoraAfectacionTemp.getSector() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar el Sector.");
                return;
            }
            if (valoraAfectacionTemp.getManzana() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar la Manzana.");
                return;
            }
            valoraAfectacionTemp = (ValoraAfectacion) manager.persist(valoraAfectacionTemp);
            CatPredio predio = catastroServices.getPredioByClaveCatAnt(valoraAfectacionTemp.getCodCatastralPredio(), "A");
            // LANZAMOS EL EVENTO DE VALORACION DEL PREDIO
            this.valorarPredio(predio);
            JsfUti.update("frmValoraClaves");
            JsfUti.messageInfo(null, "Informacion", "Registros almacenados correctamente");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error el guardar", e);
            JsfUti.messageFatal(null, "Error en transaccion", e.getMessage());
        }
        ver = true;
    }

    /**
     * Guarda o actualiza un nuevo eje comercial.
     */
    public void guardarCatalogoEjeComerciales() {
        try {
            if (!this.agregoObservacion) {
                JsfUti.messageInfo(null, "Error", "Debe ingresar la observacion");
                return;
            }
            if (ejeComercialTemp == null) {
                JsfUti.messageError(null, "Error",
                        "Ocurrio un error en el registro seleccionado, cierre el dialogo y vuelva abrirlo.");
                return;
            }

            if (ejeComercialTemp.getCodigo() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar el Codigo.");
                return;
            }
            if (ejeComercialTemp.getDescripcion() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar la Descripción.");
                return;
            }
            if (ejeComercialTemp.getValor() == null) {
                JsfUti.messageError(null, "Advertencia", "Debe ingresar el Valor.");
                return;
            }

            manager.persist(ejeComercialTemp);
            JsfUti.update("frmValoraClaves");
            JsfUti.messageInfo(null, "Informacion", "Registros almacenados correctamente");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error el guardar", e);
            JsfUti.messageFatal(null, "Error en transaccion", e.getMessage());
        }
        ver = true;
    }

    public void agregarObservaciones(int tipo) {
        if (this.observacion == null) {
            JsfUti.messageInfo(null, "Error", "Debe ingresar la observacion");
            return;
        }
        if (this.observacion.trim().length() == 0) {
            JsfUti.messageInfo(null, "Error", "Debe ingresar la observacion");
            return;
        }
        observaciones = new ArrayList<>();
        observacion = (userSession.getName_user() + " >> " + Utils.dateFormatPattern("dd-MM-yyyy HH:ss", new Date()) + " >> " + observacion);
//        observaciones.add(observacion);
        switch (tipo) {
            case 1:
                if (valoraMzTemp.getObservaciones() == null) {
                    valoraMzTemp.setObservaciones("");
                }
                valoraMzTemp.setObservaciones(observacion + "\n" + valoraMzTemp.getObservaciones());
                if (valoraMzTemp.getObservaciones() != null) {
                    observaciones.addAll(Arrays.asList(valoraMzTemp.getObservaciones().split("\n")));
                }
                break;
            case 2:
                if (valoraAfectacionTemp.getObservaciones() == null) {
                    valoraAfectacionTemp.setObservaciones("");
                }
                valoraAfectacionTemp.setObservaciones(observacion + "\n" + valoraAfectacionTemp.getObservaciones());
                if (valoraAfectacionTemp.getObservaciones() != null) {
                    observaciones.addAll(Arrays.asList(valoraAfectacionTemp.getObservaciones().split("\n")));
                }
                break;
            case 3:
                if (ejeComercialPredioTemp.getObservaciones() == null) {
                    ejeComercialPredioTemp.setObservaciones("");
                }
                ejeComercialPredioTemp.setObservaciones(observacion + "\n" + ejeComercialPredioTemp.getObservaciones());
                if (ejeComercialPredioTemp.getObservaciones() != null) {
                    observaciones.addAll(Arrays.asList(ejeComercialPredioTemp.getObservaciones().split("\n")));
                }
                break;
            case 4:
                if (ejeComercialTemp.getObservaciones() == null) {
                    ejeComercialTemp.setObservaciones("");
                }
                ejeComercialTemp.setObservaciones(observacion + "\n" + ejeComercialTemp.getObservaciones());
                if (ejeComercialTemp.getObservaciones() != null) {
                    observaciones.addAll(Arrays.asList(ejeComercialTemp.getObservaciones().split("\n")));
                }
                break;
        }
        this.agregoObservacion = true;
        observacion = null;
    }

    /**
     * Busca el registro del predio en la base de datos por la clave primaria
     *
     * @param idPredio
     * @return
     */
    public CatPredio getPredio(BigInteger idPredio) {
        if (idPredio != null) {
            return catastroServices.getPredioId(idPredio.longValue());
        }
        return null;
    }

    /**
     * Calcula la afectacion del predio
     */
    public void calcularAfectacion() {
        if (java.util.Objects.isNull(valoraAfectacionTemp.getValorCalculado())) {
            return;
        }
        if (java.util.Objects.isNull(valoraAfectacionTemp.getCosto())) {
            return;
        }
        valoraAfectacionTemp.setAfectacion(valoraAfectacionTemp.getValorCalculado().divide(valoraAfectacionTemp.getCosto(), 6, RoundingMode.HALF_UP));
    }

    public void generarClaveMZ() {
        valoraMzTemp.setClaveTotal(claveTotal(valoraMzTemp.getParroquia(), valoraMzTemp.getZona(),
                valoraMzTemp.getSector(), valoraMzTemp.getManzana()));
    }

    /**
     * Genera la clave de una manzana.
     */
    public void generarClaveaAF() {
        valoraAfectacionTemp.setCodCatastralPredio(claveTotal(valoraAfectacionTemp.getParroquia(),
                valoraAfectacionTemp.getZona(), valoraAfectacionTemp.getSector(), valoraAfectacionTemp.getManzana()));
    }

    /**
     * Concatena las variables para generar la clave.
     *
     * @param parraquia codigo de parroqia de 2 digitos
     * @param zona codigo de zona de 2 digitos
     * @param sector codigo de sector de 2 digitos
     * @param manzana codigo de manzana de 2 digitos
     * @return
     */
    public String claveTotal(String parraquia, String zona, String sector, String manzana) {
        return Utils.completarCadenaConCeros(SisVars.PROVINCIA + "", 2)
                + Utils.completarCadenaConCeros(SisVars.CANTON + "", 2) + Utils.isEmpty(parraquia) + Utils.isEmpty(zona)
                + Utils.isEmpty(sector) + Utils.isEmpty(manzana);
    }

    /**
     * Elimina el registro de la base datos.
     *
     * @param mz Manazana a eliminar
     */
    public void eliminarMz(ValoraMnz mz) {
        manager.delete(mz);
    }

    /**
     * Elimina el registro de la base datos.
     *
     * @param eje Eje comercial a eliminar
     */
    public void eliminarEje(EjeComercialPredio eje) {
        manager.delete(eje);
    }

    /**
     * Elimina el registro de la base datos.
     *
     * @param afect Valora afectacion a eliminar
     */
    public void eliminarAfect(ValoraAfectacion afect) {
        manager.delete(afect);
    }

    /**
     * Elimina un eje comercial.
     *
     * @param afect Eje comercial a eliminar de db.
     */
    public void eliminarEjeComerc(EjeComercial afect) {
        manager.delete(afect);
    }

    /**
     * Consulta todas los ejecomerciales.
     *
     * @return
     */
    public List<EjeComercial> getCatalogoEjes() {
        return (List<EjeComercial>) ejeComercialLazy.getWrappedData();
    }

    public Boolean getVer() {
        return ver;
    }

    public void setVer(Boolean ver) {
        this.agregoObservacion = false;
        this.ver = ver;
    }

    public BaseLazyDataModel<ValoraMnz> getValoraMzLazy() {
        return valoraMzLazy;
    }

    public void setValoraMzLazy(BaseLazyDataModel<ValoraMnz> valoraMzLazy) {
        this.valoraMzLazy = valoraMzLazy;
    }

    public ValoraMnz getValoraMzTemp() {
        if (valoraMzTemp == null) {
            valoraMzTemp = new ValoraMnz();
        }
        return valoraMzTemp;
    }

    public void setValoraMzTemp(ValoraMnz valoraMzTemp) {
        this.valoraMzTemp = valoraMzTemp;
    }

    public BaseLazyDataModel<EjeComercialPredio> getEjeComercialPrediosLazy() {
        return ejeComercialPrediosLazy;
    }

    public void setEjeComercialPrediosLazy(BaseLazyDataModel<EjeComercialPredio> ejeComercialPrediosLazy) {
        this.ejeComercialPrediosLazy = ejeComercialPrediosLazy;
    }

    public EjeComercialPredio getEjeComercialPredioTemp() {
        if (ejeComercialPredioTemp == null) {
            ejeComercialPredioTemp = new EjeComercialPredio();
        }

        return ejeComercialPredioTemp;
    }

    public void setEjeComercialPredioTemp(EjeComercialPredio ejeComercialPredioTemp) {
        this.ejeComercialPredioTemp = ejeComercialPredioTemp;
    }

    public BaseLazyDataModel<ValoraAfectacion> getValoraAfectacionsLazy() {
        return valoraAfectacionsLazy;
    }

    public void setValoraAfectacionsLazy(BaseLazyDataModel<ValoraAfectacion> valoraAfectacionsLazy) {
        this.valoraAfectacionsLazy = valoraAfectacionsLazy;
    }

    public ValoraAfectacion getValoraAfectacionTemp() {
        if (valoraAfectacionTemp == null) {
            valoraAfectacionTemp = new ValoraAfectacion();
        }

        return valoraAfectacionTemp;
    }

    public void setValoraAfectacionTemp(ValoraAfectacion valoraAfectacionTemp) {
        this.valoraAfectacionTemp = valoraAfectacionTemp;
    }

    public BaseLazyDataModel<EjeComercial> getEjeComercialLazy() {
        return ejeComercialLazy;
    }

    public void setEjeComercialLazy(BaseLazyDataModel<EjeComercial> ejeComercialLazy) {
        this.ejeComercialLazy = ejeComercialLazy;
    }

    public EjeComercial getEjeComercialTemp() {
        if (ejeComercialTemp == null) {
            ejeComercialTemp = new EjeComercial();
        }

        return ejeComercialTemp;
    }

    public void setEjeComercialTemp(EjeComercial ejeComercialTemp) {
        this.ejeComercialTemp = ejeComercialTemp;
    }

    public List<Integer> getEstados() {
        return estados;
    }

    public void setEstados(List<Integer> estados) {
        this.estados = estados;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }

}
