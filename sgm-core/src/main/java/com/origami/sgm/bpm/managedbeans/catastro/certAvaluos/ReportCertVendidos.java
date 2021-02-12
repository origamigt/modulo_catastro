/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro.certAvaluos;

import com.origami.sgm.bpm.models.ModelUsuarios;
import com.origami.sgm.entities.AclRol;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatCertificadoAvaluo;
import com.origami.sgm.entities.GeDepartamento;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import util.JsfUti;
import util.Utils;

/**
 *
 * @author TIC
 */
@Named
@ViewScoped
public class ReportCertVendidos implements Serializable {

    private static final Logger LOG = Logger.getLogger(ReportCertVendidos.class.getName());

    private BaseLazyDataModel<CatCertificadoAvaluo> lazyCertificados;
    private Date fechaInicio;
    private Date fechaFin;
    private String usuario;
    private List<AclUser> usuariosCatastros;
    private List<ModelUsuarios> model;
    @Inject
    private CatastroServices cs;

    @PostConstruct
    public void initView() {
        if (!JsfUti.isAjaxRequest()) {
            lazyCertificados = new BaseLazyDataModel<CatCertificadoAvaluo>(CatCertificadoAvaluo.class, new String[]{"fecha"},
                    new Object[]{new Date()}, "numCert", "DESC");
            Calendar ffin = Calendar.getInstance();
            Date tempFechaInicio = ffin.getTime();
            ffin.add(Calendar.DAY_OF_MONTH, 1);
            model = cs.getCertificadosPorUsuario(Utils.dateFormatPattern("yyyy-MM-dd", tempFechaInicio), Utils.dateFormatPattern("yyyy-MM-dd", ffin.getTime()));
            fechaInicio = new Date();
        }
    }

    public void parametros() {
        try {
            GeDepartamento dptoByName = cs.getDptoByName("CATASTRO");
            usuariosCatastros = new ArrayList<>();
            for (AclRol aclRol : dptoByName.getAclRolCollection()) {
                if (aclRol.getNombre().equalsIgnoreCase("certificados")
                        || aclRol.getNombre().contains("certificado")) {
                    for (AclUser aclUser : aclRol.getAclUserCollection()) {
                        if (aclUser.getSisEnabled()) {
                            usuariosCatastros.add(aclUser);
                        }
                    }

                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al buscar Usuarios", e);
        }
        fechaInicio = new Date();
        fechaFin = new Date();
        JsfUti.executeJS("PF('dlgReporte').show()");
    }

    public void generarReporte() {
        Calendar ffin = Calendar.getInstance();
        ffin.setTime(fechaFin);
        ffin.add(Calendar.DAY_OF_MONTH, 1);
        if (usuario == null || usuario.trim().isEmpty()) {
            model = cs.getCertificadosPorUsuario(Utils.dateFormatPattern("yyyy-MM-dd", fechaInicio), Utils.dateFormatPattern("yyyy-MM-dd", fechaFin));
        } else {
            model = cs.getCertificadosPorUsuario(Utils.dateFormatPattern("yyyy-MM-dd", fechaInicio), Utils.dateFormatPattern("yyyy-MM-dd", fechaFin), usuario);
        }
    }

    public BaseLazyDataModel<CatCertificadoAvaluo> getLazyCertificados() {
        return lazyCertificados;
    }

    public void setLazyCertificados(BaseLazyDataModel<CatCertificadoAvaluo> lazyCertificados) {
        this.lazyCertificados = lazyCertificados;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public List<AclUser> getUsuariosCatastros() {
        return usuariosCatastros;
    }

    public void setUsuariosCatastros(List<AclUser> usuariosCatastros) {
        this.usuariosCatastros = usuariosCatastros;
    }

    public List<ModelUsuarios> getModel() {
        return model;
    }

    public void setModel(List<ModelUsuarios> model) {
        this.model = model;
    }

}
