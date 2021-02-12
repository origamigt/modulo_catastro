/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.entities;

import com.google.gson.annotations.Expose;
import com.origami.annotations.Report;
import com.origami.annotations.ReportField;
import com.origami.enums.FieldType;
import com.origami.sgm.database.SchemasConfig;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Angel Navarro
 */
@Report(description = "Colindantes")
@Entity
@Table(name = "cat_predio_linderos", schema = SchemasConfig.APP1)
@XmlRootElement
@SequenceGenerator(name = "cat_predio_linderos_id_seq", sequenceName = SchemasConfig.APP1 + ".cat_predio_linderos_id_seq", allocationSize = 1)
public class CatPredioLinderos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_predio_linderos_id_seq")
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @Expose
    private Long id;
    @Basic(optional = false)
    @NotNull
    @JoinColumn(name = "predio", nullable = false, referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @ReportField(description = "Predio", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CatPredio predio;
    @JoinColumn(name = "orientacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Orientación", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem orientacion;
    @Size(max = 2147483647)
    @Column(name = "colindante")
    @Expose
    @ReportField(description = "Con")
    private String colindante;
    @Size(max = 1)
    @Column(name = "estado", length = 1)
    @Expose
    private String estado;
    @JoinColumn(name = "predio_colindante", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @ReportField(description = "Con (Predio colindante)", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CatPredio predioColindante;

    public CatPredioLinderos() {
    }

    public CatPredioLinderos(Long id) {
        this.id = id;
    }

    public CatPredioLinderos(Long id, CatPredio predio) {
        this.id = id;
        this.predio = predio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColindante() {
        return colindante;
    }

    public void setColindante(String colindante) {
        this.colindante = colindante;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public CatPredio getPredioColindante() {
        return predioColindante;
    }

    public void setPredioColindante(CatPredio predioColindante) {
        this.predioColindante = predioColindante;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public CtlgItem getOrientacion() {
        return orientacion;
    }

    public void setOrientacion(CtlgItem orientacion) {
        this.orientacion = orientacion;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CatPredioLinderos)) {
            return false;
        }
        CatPredioLinderos other = (CatPredioLinderos) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CatPredioLinderos[ id=" + id + " ]";
    }

}
