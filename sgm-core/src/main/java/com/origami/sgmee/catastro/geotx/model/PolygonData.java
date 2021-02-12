/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx.model;

import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Modelo de datos para guardar los datos de la tabla 'predios_tx'.
 *
 * @author Fernando
 */
public class PolygonData implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer gid;
    private String codigo;
    private String tipo;
    private Short numeracion;
    private BigDecimal area;
    private Boolean habilitado;
    private Integer manzana;

    private List<BloqueGeoData> bloques;

    public PolygonData() {
    }

    public PolygonData(Integer gid, String codigo, Short numeracion, BigDecimal area) {
        this.gid = gid;
        this.codigo = codigo;
        this.numeracion = numeracion;
        this.area = area;
    }

    public PolygonData(Integer gid, String codigo, Short numeracion, BigDecimal area, Boolean habilitado) {
        this.gid = gid;
        this.codigo = codigo;
        this.numeracion = numeracion;
        this.area = area;
        this.habilitado = habilitado;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Short getNumeracion() {
        return numeracion;
    }

    public void setNumeracion(Short numeracion) {
        this.numeracion = numeracion;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public Boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(Boolean habilitado) {
        this.habilitado = habilitado;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gid == null) ? 0 : gid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PolygonData other = (PolygonData) obj;
        if (gid == null) {
            if (other.gid != null) {
                return false;
            }
        } else if (!gid.equals(other.gid)) {
            return false;
        }
        return true;
    }

    public List<BloqueGeoData> getBloques() {
        return bloques;
    }

    public void setBloques(List<BloqueGeoData> bloques) {
        this.bloques = bloques;
    }

    public Integer getManzana() {
        return manzana;
    }

    public void setManzana(Integer manzana) {
        this.manzana = manzana;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

}
