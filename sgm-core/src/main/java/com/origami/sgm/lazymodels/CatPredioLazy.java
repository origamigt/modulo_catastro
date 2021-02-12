/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.lazymodels;

import com.origami.sgm.bpm.models.CatPredioModel;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.models.EstadosPredio;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Angel Navarro, CarlosLoorVargas
 */
public class CatPredioLazy extends BaseLazyDataModel<CatPredio> {

    private String estado;
    private Boolean tieneAvaluoPropiedad = false;
    private BigInteger numPredio;
    private List<CatPredioPropietario> propietarios;
    private CatPredioModel model;
    private Boolean esPropiedadHorizontal = false;
    private String tipoPredio = null;
    private Boolean soloActivos = false;

    public CatPredioLazy() {
        super(CatPredio.class, "claveCat", "ASC");
    }

//    public CatPredioLazy() {
//        super(CatPredio.class, "parroquia", "ASC", "zona", "ASC", "sector", "ASC", "mz", "ASC");
//    }
//
    public CatPredioLazy(String estado) {
        super(CatPredio.class);
        this.estado = estado;
    }

    public CatPredioLazy(Boolean tieneAvaluoPropiedad) {
        super(CatPredio.class);
        this.tieneAvaluoPropiedad = tieneAvaluoPropiedad;
    }

    public CatPredioLazy(BigInteger numPredio, String estado) {
        super(CatPredio.class);
        this.estado = estado;
        this.numPredio = numPredio;
    }

    public CatPredioLazy(List propietarios) {
        super(CatPredio.class);
        this.propietarios = propietarios;
    }

    public CatPredioLazy(CatPredioModel model) {
        super(CatPredio.class, "numPredio", "DESC");
        this.model = model;
    }

    @Override
    public void criteriaFilterSetup(Criteria crit, Map<String, Object> filters) throws Exception {
        // LLAMAMOS AL ME  QUE REALIZA LA BUSQUEDA DE CUALQUIER PROPIEDAD  
        this.findPropertyFilter(crit, filters);

        // SI ES FALSE LA VARIABLE SOLOACTIVOS FILTRA  S LOS PREDIOS
        if (!soloActivos) {
            if (estado != null) {
                crit.add(Restrictions.eq("estado", estado));
                crit.add(Restrictions.ne("estado", EstadosPredio.TEMPORAL));
            } else {
                crit.add(Restrictions.ne("estado", "R"));
                crit.add(Restrictions.ne("estado", EstadosPredio.TEMPORAL));
            }
        } else { // CASO CONTRARIO SOLO LOS QUE ESTAN ACTIVOS Y PENDIENTES
            if (Objects.isNull(getColunmEstado())) {
                crit.add(Restrictions.in(getColunmEstado(), EstadosPredio.ACTIVOS_TEMPORALES));
            } else {
                crit.add(Restrictions.in(getColunmEstado(), this.getValueEstados()));
            }
        }
        if (tieneAvaluoPropiedad == true) {
            crit.add(Restrictions.isNotNull("avaluoMunicipal"));
            crit.add(Restrictions.ne("avaluoMunicipal", new BigDecimal("0.00")));
        }
        if (numPredio != null) {
            crit.add(Restrictions.eq("numPredio", numPredio));
        }
        if (propietarios != null && !propietarios.isEmpty()) {
            crit.createCriteria("catPredioPropietarioCollection").add(Restrictions.in("id", getIdPropietarios(propietarios)));
//            crit.add(Restrictions.in("catPredioPropietarioCollection", propietarios));
        }

        try {
            if (esPropiedadHorizontal) {
                crit.add(Restrictions.eq("propiedadHorizontal", Boolean.TRUE));
            }
            if (model != null) {
                if (model.getNumPredio() != null && model.getNumPredio().compareTo(BigInteger.ZERO) > 0) {
                    crit.add(Restrictions.eq("numPredio", model.getNumPredio()));
                }
                if (model.getCdla() > 0) {
                    crit.add(Restrictions.eq("cdla", model.getCdla()));
                }
                if (model.getMzDiv() > 0) {
                    crit.add(Restrictions.eq("mzdiv", model.getMzDiv()));
                }
                if (model.getMzUrb() != null) {
                    crit.add(Restrictions.eq("urbMz", model.getMzUrb()));
                }
                if (model.getSlUrb() != null) {
                    crit.add(Restrictions.eq("urbSolarnew", model.getSlUrb()));
                }
                if (model.getDivisionUrb() != null) {
                    crit.add(Restrictions.eq("divisionUrb", model.getDivisionUrb()));
                }
                if (model.getNumDepartamento() != null) {
                    crit.add(Restrictions.eq("numDepartamento", model.getNumDepartamento()));
                }
                if (model.getCiudadela() != null) {
                    crit.createCriteria("ciudadela").add(Restrictions.eq("id", model.getCiudadela().getId()));
                }
                if (model.getPredialAnterior() != null) {
                    crit.add(Restrictions.ilike("predialant", "%" + model.getPredialAnterior() + "%"));
                }
                if (model.getPredialAnt() != null) {
                    crit.add(Restrictions.ilike("predialantAnt", "%" + model.getPredialAnt() + "%"));
                }
                ///////

                if (model.getParroquiaShort() > 0) {
                    crit.add(Restrictions.eq("parroquia", model.getParroquiaShort()));
                }
                if (model.getZona() > 0) {
                    crit.add(Restrictions.eq("zona", model.getZona()));
                }
                if (model.getSector() > 0) {
                    crit.add(Restrictions.eq("sector", model.getSector()));
                }
                if (model.getMz() > 0) {
                    crit.add(Restrictions.eq("mz", model.getMz()));
                }
                if (model.getSolar() > 0) {
                    crit.add(Restrictions.eq("solar", model.getSolar()));
                }
                if (model.getPiso() > 0) {
                    crit.add(Restrictions.eq("piso", model.getPiso()));
                }
                if (model.getBloque() > 0) {
                    crit.add(Restrictions.eq("bloque", model.getBloque()));
                }
                if (model.getUnidad() > 0) {
                    crit.add(Restrictions.eq("unidad", model.getUnidad()));
                }
                if (model.getLote() > 0) {
                    crit.add(Restrictions.eq("lote", model.getLote()));
                }
                if (model.getCodigoPredial() != null && model.getCodigoPredial().trim().length() > 0) {
                    crit.add(Restrictions.ilike("claveCat", "%" + model.getCodigoPredial() + "%"));
                }
            }
            if (this.tipoPredio != null) {
                crit.add(Restrictions.ilike("tipoPredio", "%" + this.tipoPredio + "%"));
            }
        } catch (HibernateException e) {
            System.out.println("PrediosLazy --> " + e.getMessage());
        }
    }

    public BigInteger getNumPredio() {
        return numPredio;
    }

    public void setNumPredio(BigInteger numPredio) {
        this.numPredio = numPredio;
    }

    public List<CatPredioPropietario> getPropietarios() {
        return propietarios;
    }

    public void setPropietarios(List<CatPredioPropietario> propietarios) {
        this.propietarios = propietarios;
    }

    public CatPredioModel getModel() {
        return model;
    }

    public void setModel(CatPredioModel model) {
        this.model = model;
    }

    public Boolean getEsPropiedadHorizontal() {
        return esPropiedadHorizontal;
    }

    public void setEsPropiedadHorizontal(Boolean esPropiedadHorizontal) {
        this.esPropiedadHorizontal = esPropiedadHorizontal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipoPredio() {
        return tipoPredio;
    }

    public void setTipoPredio(String tipoPredio) {
        this.tipoPredio = tipoPredio;
    }

    private Collection getIdPropietarios(List<CatPredioPropietario> propietarios) {
        Collection c = new ArrayList<>();
        for (CatPredioPropietario propietario : propietarios) {
            c.add(propietario.getId());
        }
        return c;
    }

    public Boolean getSoloActivos() {
        return soloActivos;
    }

    public void setSoloActivos(Boolean soloActivos) {
        this.soloActivos = soloActivos;
    }

}
