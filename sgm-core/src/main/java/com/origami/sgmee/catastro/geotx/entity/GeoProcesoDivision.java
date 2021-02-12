/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx.entity;

import com.origami.sgm.database.SchemasConfig;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.OrderBy;
import util.Utils;

/**
 * Permite almacenar las claves que estan en proceso de fracionamiento.
 *
 * @author Fernando
 */
@SequenceGenerator(name = "geo_proceso_division_id_seq", sequenceName = SchemasConfig.APP1 + ".geo_proceso_division_id_seq", allocationSize = 1)
@Entity
@Table(name = "geo_proceso_division", schema = SchemasConfig.APP1)
public class GeoProcesoDivision implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "geo_proceso_division_id_seq")
    private Long id;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "processid")
//    private Long processId;
    private BigInteger processId;

    @OrderBy(clause = "numeracion ASC")
    @OneToMany(mappedBy = "procesoDiv")
    private List<GeoPrediosDivididos> predios;

    @Column(name = "activo")
    private Boolean activo;

    public GeoProcesoDivision() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public List<GeoPrediosDivididos> getPredios() {
        return predios;
    }

    public void setPredios(List<GeoPrediosDivididos> predios) {
        this.predios = predios;
    }

    public BigInteger getProcessId() {
        return processId;
    }

    public void setProcessId(BigInteger processId) {
        this.processId = processId;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public GeoPrediosDivididos findClaveCatPredio(String clavecat) {
        if (Utils.isNotEmpty(predios)) {
            for (GeoPrediosDivididos predio : predios) {
                if (clavecat != null) {
                    if (predio.getCodigoNuevo() != null) {
                        if (predio.getCodigoNuevo().equalsIgnoreCase(clavecat)) {
                            return predio;
                        }
                    }
                } else {
                    System.out.println("Clave es nula");
                }
            }
        }
        return null;
    }

}
