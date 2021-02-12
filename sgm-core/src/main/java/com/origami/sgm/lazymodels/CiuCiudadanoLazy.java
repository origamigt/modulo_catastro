/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.lazymodels;

import com.origami.catastroextras.entities.ibarra.CiuCiudadano;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/**
 * Lazy que carga el listado de {@link CiuCiudadano} por demanda.
 *
 * @author Joao Sanga
 */
public class CiuCiudadanoLazy extends BaseLazyDataModelIb<CiuCiudadano> {

    public CiuCiudadanoLazy() {
        super(CiuCiudadano.class, "ciuCodigo", "DESC");
    }

    @Override
    public void criteriaFilterSetup(Criteria crit, Map<String, Object> filters) throws Exception {
        if (filters.containsKey("ciuCedRuc")) {
            crit.add(Restrictions.ilike("ciuCedRuc", "%" + filters.get("ciuCedRuc").toString().trim() + "%"));
        }
        if (filters.containsKey("ciuApellidoPat")) {
            crit.add(Restrictions.ilike("ciuApellidoPat", "%" + filters.get("ciuApellidoPat").toString().trim().replaceAll(" ", "%") + "%"));
        }
        if (filters.containsKey("ciuApellidoMat")) {
            crit.add(Restrictions.ilike("ciuApellidoMat", "%" + filters.get("ciuApellidoMat").toString().trim().replaceAll(" ", "%") + "%"));
        }
        if (filters.containsKey("ciuPrimerNombre")) {
            crit.add(Restrictions.ilike("ciuPrimerNombre", "%" + filters.get("ciuPrimerNombre").toString().trim().replaceAll(" ", "%") + "%"));
        }
        if (filters.containsKey("ciuSegundoNombre")) {
            crit.add(Restrictions.ilike("ciuSegundoNombre", "%" + filters.get("ciuSegundoNombre").toString().trim().replaceAll(" ", "%") + "%"));
        }
        if (filters.containsKey("ciuNombreCompleto")) {
            crit.add(Restrictions.ilike("ciuNombreCompleto", "%" + filters.get("ciuNombreCompleto").toString().trim().replaceAll(" ", "%") + "%"));
        }
    }

}
