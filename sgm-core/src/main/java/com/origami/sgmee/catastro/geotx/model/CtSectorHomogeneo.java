/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx.model;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author ANGEL NAVARRO
 */
public class CtSectorHomogeneo implements Serializable {

    private Integer gid;
    private String fcode;
    private Long sectorHomogeneo;
    private Integer referecia;

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getFcode() {
        return fcode;
    }

    public void setFcode(String fcode) {
        this.fcode = fcode;
    }

    public Long getSectorHomogeneo() {
        return sectorHomogeneo;
    }

    public void setSectorHomogeneo(Long sectorHomogeneo) {
        this.sectorHomogeneo = sectorHomogeneo;
    }

    public Integer getReferecia() {
        return referecia;
    }

    public void setReferecia(Integer referecia) {
        this.referecia = referecia;
    }

    public CtSectorHomogeneo(Integer gid, String fcode, Long sectorHomogeneo, Integer referecia) {
        this.gid = gid;
        this.fcode = fcode;
        this.sectorHomogeneo = sectorHomogeneo;
        this.referecia = referecia;
    }

    public CtSectorHomogeneo() {
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.gid);
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
        final CtSectorHomogeneo other = (CtSectorHomogeneo) obj;
        if (!Objects.equals(this.gid, other.gid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CtSectorHomogeneo{" + "gid=" + gid + ", sectorHomogeneo=" + sectorHomogeneo + '}';
    }

}
