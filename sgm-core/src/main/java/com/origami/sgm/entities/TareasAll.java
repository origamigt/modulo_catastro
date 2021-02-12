/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.entities;

import com.origami.sgm.database.SchemasConfig;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author ANGEL NAVARRO
 */
@Entity
@Table(name = "tareas_all", schema = SchemasConfig.APP1)
@NamedQueries({
    @NamedQuery(name = "TareasAll.findAll", query = "SELECT t FROM TareasAll t")})
public class TareasAll implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "proc_inst_id_")
    private String procInstId;
    @Column(name = "id_")
    private String id;
    @Column(name = "id_tipo_tramite")
    private BigInteger idTipoTramite;
    private String descripcion;
    @Column(name = "numero_tramite")
    private String numeroTramite;
    @Column(name = "numero_segimiento")
    private BigInteger numeroSegimiento;
    @JoinColumn(name = "id_tramite", referencedColumnName = "id_tramite")
    @ManyToOne(fetch = FetchType.LAZY)
    private HistoricoTramites idTramite;
    @Column(length = 15)
    private String estado;
    @Column(name = "task_def_key_")
    private String taskDefKey;
    @Column(name = "name_")
    private String name;
    @Column(name = "assignee_")
    private String assignee;
    @Column(name = "start_time_")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Column(name = "end_time_")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    @Column(name = "form_key_")
    private String formKey;
    @Column(name = "delete_reason_")
    private String deleteReason;

    public TareasAll() {
    }

    public BigInteger getIdTipoTramite() {
        return idTipoTramite;
    }

    public void setIdTipoTramite(BigInteger idTipoTramite) {
        this.idTipoTramite = idTipoTramite;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNumeroTramite() {
        return numeroTramite;
    }

    public void setNumeroTramite(String numeroTramite) {
        this.numeroTramite = numeroTramite;
    }

    public BigInteger getNumeroSegimiento() {
        return numeroSegimiento;
    }

    public void setNumeroSegimiento(BigInteger numeroSegimiento) {
        this.numeroSegimiento = numeroSegimiento;
    }

    public HistoricoTramites getIdTramite() {
        return idTramite;
    }

    public void setIdTramite(HistoricoTramites idTramite) {
        this.idTramite = idTramite;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId;
    }

    public String getTaskDefKey() {
        return taskDefKey;
    }

    public void setTaskDefKey(String taskDefKey) {
        this.taskDefKey = taskDefKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    public void setDeleteReason(String deleteReason) {
        this.deleteReason = deleteReason;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.id);
        return hash;
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
        final TareasAll other = (TareasAll) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TareasAll{" + "id=" + id + ", idTipoTramite=" + idTipoTramite + ", descripcion=" + descripcion + ", numeroTramite=" + numeroTramite + ", numeroSegimiento=" + numeroSegimiento + ", idTramite=" + idTramite + ", estado=" + estado + ", procInstId=" + procInstId + ", taskDefKey=" + taskDefKey + ", name=" + name + ", assignee=" + assignee + ", startTime=" + startTime + ", endTime=" + endTime + ", formKey=" + formKey + '}';
    }
    
}
