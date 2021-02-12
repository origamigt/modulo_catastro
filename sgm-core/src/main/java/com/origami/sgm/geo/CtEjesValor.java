/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.geo;

import java.util.Objects;

/**
 *
 * @author Angel Navarro
 */
public class CtEjesValor implements java.io.Serializable {

    private Integer gid;
    private String descripcion;
    private Double valor;
    private String codigo;
    private String estado;
    private Double buffer;

    public CtEjesValor() {

    }

    public CtEjesValor(Integer gid, String descripcion, Double valor, String codigo, String estado, Double buffer) {
        this.gid = gid;
        this.descripcion = descripcion;
        this.valor = valor;
        this.codigo = codigo;
        this.estado = estado;
        this.buffer = buffer;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Double getBuffer() {
        return buffer;
    }

    public void setBuffer(Double buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CtEjesValor)) {
            return false;
        }

        return java.util.Objects.equals(this, obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.gid);
        return hash;
    }

    @Override
    public String toString() {
        return descripcion;
    }

}
