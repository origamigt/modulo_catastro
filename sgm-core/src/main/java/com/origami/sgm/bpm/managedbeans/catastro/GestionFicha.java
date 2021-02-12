/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro;

import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import util.Faces;
import util.JsfUti;

/**
 *
 * @author ANGEL NAVARRO
 */
@Named
@ViewScoped
public class GestionFicha extends PredioUtil implements Serializable {

    private static final Long serialVersionUID = 1L;
    private static final Logger logx = Logger.getLogger(GestionFicha.class.getName());
    @Inject
    private UserSession sess;
    @javax.inject.Inject
    private Entitymanager manager;
    @Inject
    private ServletSession ss;

    @javax.inject.Inject
    protected CatastroServices catast;

    @PostConstruct
    protected void load() {
        this.init();
        if (predio == null) {
            predio = new CatPredio();
        }
        if (SisVars.PROVINCIA != null) {
            predio.setProvincia(SisVars.PROVINCIA);
        }
        if (SisVars.CANTON != null) {
            predio.setCanton(SisVars.CANTON);
        }
        predio.setBloque(Short.valueOf("0"));
        predio.setUnidad(Short.valueOf("0"));
        predio.setPiso(Short.valueOf("0"));
    }

    public void registrar() {
        if (estaDibujado()) {
            if (predio.getTipoConjunto() == null || predio.getCiudadela() == null) {
                Faces.messageInfo(null, "Datos Invalidos", "Los datos marcados como (*) son Obligatorios");
                JsfUti.update("frmMain");
                return;
            } else {
                if (predio.getProvincia() > 0 && predio.getCanton() > 0 && predio.getParroquia() > 0
                        && predio.getZona() > 0 && predio.getSector() > 0 && predio.getMz() > 0
                        && predio.getSolar() > 0) {
                    predio.setUsuarioCreador(new AclUser(sess.getUserId()));
                    predio.setInstCreacion(new Date());
                    predio.setEstado("A");
                    predio.setLote(predio.getSolar());
                    predio.setMzdiv(new Short("0"));
                    predio.setPropiedadHorizontal(false);
                    this.setNamePredioByCiudadela();
                    if (getCatas().existePredio(predio) == true) {
                        predio = new CatPredio();
                        Faces.messageInfo(null, "Predio ya ha sido anteriormente registrado", "");
                        JsfUti.update("frmMain");
                        return;
                    }
                    CatPredio p = new CatPredio();
                    p = predio;
                    predio = this.registrarPredio();
                    if (predio != null && predio.getId() != null) {
                        if (predio.getNumPredio() != null || predio.getNumPredio().compareTo(BigInteger.ZERO) <= 0) {
                            this.predio = getCatas().generarNumPredio(predio);
                        }
                        Faces.messageInfo(null, "Nota!", "Matricula Inmobiliaria registrada satisfactoriamente ");
                        JsfUti.executeJS("PF('dlgMatricula').show()");
                    } else {
                        predio = p;
                        JsfUti.update("frmMain");
                    }
                } else {
                    Faces.messageInfo(null, "Datos Invalidos", "Los datos marcados como (*) son Obligatorios");
                }
            }
        } else {
            Faces.messageInfo(null, "No dibujado !", " predio no se ecuentra dibujado.");
        }
    }

    public boolean estaDibujado() {
        return true;
    }

    public void continuar() {
        if (predio.getId() != null) {
            ss.instanciarParametros();
            ss.agregarParametro("idPredio", predio.getId());
            ss.agregarParametro("edit", true);
            ss.agregarParametro("numPredio", predio.getNumPredio());
            Faces.redirectFacesNewTab("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
            predio = new CatPredio();
        } else {
            Faces.messageWarning(null, "Advertencia!", "El predio no registra ninguna matricula inmobiliaria, revise que los datos ingresados sean correctos");
        }
    }

}
