/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.lazymodels;


/**
 *
 * @author TIC
 */
import com.origami.sgm.entities.CatPredioPropietario;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class CatPredioPropietarioLazy extends BaseLazyDataModel<CatPredioPropietario> {
  
    public CatPredioPropietarioLazy(){
    super(CatPredioPropietario.class, "id", "ASC");
    
    
}
    @Override
    public void criteriaFilterSetup(Criteria crit, Map<String, Object> filters) throws Exception {
        if (filters.containsKey("ciuCedRuc")) {
            crit.add(Restrictions.ilike("ciuCedRuc", "%" + filters.get("ciuCedRuc").toString().trim() + "%"));
        }
       /* 
        if (filters.containsKey("nombrescompletos")) {
            crit.add(Restrictions.ilike("nombrescompletos", "%" + filters.get("nombrescompletos").toString().trim() + "%"));
        }
        */
    }
}
