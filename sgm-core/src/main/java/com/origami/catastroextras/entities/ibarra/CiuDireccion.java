/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import com.origami.extras.util.IbarraSchemas;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Origami13
 */
@Entity
@Table(schema = IbarraSchemas.PCIUDADANO, name = "ciu_direccion", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"di_ciudadano", "di_ciudad", "di_calle_principal", "di_numero", "di_calle_secundaria"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CiuDireccion.findAll", query = "SELECT c FROM CiuDireccion c"),
    @NamedQuery(name = "CiuDireccion.findByDiSecuencia", query = "SELECT c FROM CiuDireccion c WHERE c.diSecuencia = :diSecuencia"),
    @NamedQuery(name = "CiuDireccion.findByDiCiudad", query = "SELECT c FROM CiuDireccion c WHERE c.diCiudad = :diCiudad"),
    @NamedQuery(name = "CiuDireccion.findByDiCallePrincipal", query = "SELECT c FROM CiuDireccion c WHERE c.diCallePrincipal = :diCallePrincipal"),
    @NamedQuery(name = "CiuDireccion.findByDiNumero", query = "SELECT c FROM CiuDireccion c WHERE c.diNumero = :diNumero"),
    @NamedQuery(name = "CiuDireccion.findByDiCalleSecundaria", query = "SELECT c FROM CiuDireccion c WHERE c.diCalleSecundaria = :diCalleSecundaria"),
    @NamedQuery(name = "CiuDireccion.findByDiTipo", query = "SELECT c FROM CiuDireccion c WHERE c.diTipo = :diTipo"),
    @NamedQuery(name = "CiuDireccion.findByDiReferencia", query = "SELECT c FROM CiuDireccion c WHERE c.diReferencia = :diReferencia"),
    @NamedQuery(name = "CiuDireccion.findByDiProvincia", query = "SELECT c FROM CiuDireccion c WHERE c.diProvincia = :diProvincia"),
    @NamedQuery(name = "CiuDireccion.findByDiParroquia", query = "SELECT c FROM CiuDireccion c WHERE c.diParroquia = :diParroquia"),
    @NamedQuery(name = "CiuDireccion.findByDiDescripcion", query = "SELECT c FROM CiuDireccion c WHERE c.diDescripcion = :diDescripcion")})
public class CiuDireccion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "di_secuencia", nullable = false)
    private Integer diSecuencia;
//    @Size(max = 5)
    @Column(name = "di_ciudad", length = 5)
    private String diCiudad;
    @Size(max = 100)
    @Column(name = "di_calle_principal", length = 100)
    private String diCallePrincipal;
    @Size(max = 10)
    @Column(name = "di_numero", length = 10)
    private String diNumero;
    @Size(max = 100)
    @Column(name = "di_calle_secundaria", length = 100)
    private String diCalleSecundaria;
    @Size(max = 2)
    @Column(name = "di_tipo", length = 2)
    private String diTipo;
    @Size(max = 250)
    @Column(name = "di_referencia", length = 250)
    private String diReferencia;
    @Column(name = "di_provincia")
    private Integer diProvincia;
    @Column(name = "di_parroquia")
    private Integer diParroquia;
    @Size(max = 150)
    @Column(name = "di_descripcion", length = 150)
    private String diDescripcion;
    @JoinColumn(name = "di_ciudadano", referencedColumnName = "ciu_ced_ruc")
    @ManyToOne
    private CiuCiudadano diCiudadano;

    public CiuDireccion() {
    }

    public CiuDireccion(Integer diSecuencia) {
        this.diSecuencia = diSecuencia;
    }

    public Integer getDiSecuencia() {
        return diSecuencia;
    }

    public void setDiSecuencia(Integer diSecuencia) {
        this.diSecuencia = diSecuencia;
    }

    public String getDiCiudad() {
        return diCiudad;
    }

    public void setDiCiudad(String diCiudad) {
        this.diCiudad = diCiudad;
    }

    public String getDiCallePrincipal() {
        return diCallePrincipal;
    }

    public void setDiCallePrincipal(String diCallePrincipal) {
        this.diCallePrincipal = diCallePrincipal;
    }

    public String getDiNumero() {
        return diNumero;
    }

    public void setDiNumero(String diNumero) {
        this.diNumero = diNumero;
    }

    public String getDiCalleSecundaria() {
        return diCalleSecundaria;
    }

    public void setDiCalleSecundaria(String diCalleSecundaria) {
        this.diCalleSecundaria = diCalleSecundaria;
    }

    public String getDiTipo() {
        return diTipo;
    }

    public void setDiTipo(String diTipo) {
        this.diTipo = diTipo;
    }

    public String getDiReferencia() {
        return diReferencia;
    }

    public void setDiReferencia(String diReferencia) {
        this.diReferencia = diReferencia;
    }

    public Integer getDiProvincia() {
        return diProvincia;
    }

    public void setDiProvincia(Integer diProvincia) {
        this.diProvincia = diProvincia;
    }

    public Integer getDiParroquia() {
        return diParroquia;
    }

    public void setDiParroquia(Integer diParroquia) {
        this.diParroquia = diParroquia;
    }

    public String getDiDescripcion() {
        return diDescripcion;
    }

    public void setDiDescripcion(String diDescripcion) {
        this.diDescripcion = diDescripcion;
    }

    public CiuCiudadano getDiCiudadano() {
        return diCiudadano;
    }

    public void setDiCiudadano(CiuCiudadano diCiudadano) {
        this.diCiudadano = diCiudadano;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (diSecuencia != null ? diSecuencia.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CiuDireccion)) {
            return false;
        }
        CiuDireccion other = (CiuDireccion) object;
        if ((this.diSecuencia == null && other.diSecuencia != null) || (this.diSecuencia != null && !this.diSecuencia.equals(other.diSecuencia))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CiuDireccion[ diSecuencia=" + diSecuencia + " ]";
    }

}
