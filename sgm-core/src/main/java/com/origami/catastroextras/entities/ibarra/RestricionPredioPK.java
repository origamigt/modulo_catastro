/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

/**
 *
 * @author Origami13
 */
@Embeddable
public class RestricionPredioPK implements Serializable {

//    @Basic(optional = false)
    @Size(min = 1, max = 18)
    @Column(name = "cod_catastral_predio", nullable = false, length = 18)
    private String codCatastralPredio;
    @Basic(optional = false)
    @Column(name = "codigo_restriccion", nullable = false)
    private int codigoRestriccion;

    public RestricionPredioPK() {
    }

    public RestricionPredioPK(String codCatastralPredio, int codigoRestriccion) {
        this.codCatastralPredio = codCatastralPredio;
        this.codigoRestriccion = codigoRestriccion;
    }

    public String getCodCatastralPredio() {
        return codCatastralPredio;
    }

    public void setCodCatastralPredio(String codCatastralPredio) {
        this.codCatastralPredio = codCatastralPredio;
    }

    public int getCodigoRestriccion() {
        return codigoRestriccion;
    }

    public void setCodigoRestriccion(int codigoRestriccion) {
        this.codigoRestriccion = codigoRestriccion;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codCatastralPredio != null ? codCatastralPredio.hashCode() : 0);
        hash += (int) codigoRestriccion;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RestricionPredioPK)) {
            return false;
        }
        RestricionPredioPK other = (RestricionPredioPK) object;
        if ((this.codCatastralPredio == null && other.codCatastralPredio != null) || (this.codCatastralPredio != null && !this.codCatastralPredio.equals(other.codCatastralPredio))) {
            return false;
        }
        if (this.codigoRestriccion != other.codigoRestriccion) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RestricionPredioPK[ codCatastralPredio=" + codCatastralPredio + ", codigoRestriccion=" + codigoRestriccion + " ]";
    }

}
