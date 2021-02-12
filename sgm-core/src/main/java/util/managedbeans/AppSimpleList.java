/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.managedbeans;

import com.origami.sgm.entities.CtlgCatalogo;
import com.origami.sgm.entities.CtlgItem;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * Contiene los diferentes listados de catalogos.
 *
 * @author Anyelo
 */
@Named
@ApplicationScoped
public class AppSimpleList implements Serializable {

    private static final long serialVersionUID = 1L;

    @javax.inject.Inject
    protected Entitymanager manager;

    public List<CtlgItem> findCatalogoItem(String nameCatalogo) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("nombre", nameCatalogo);
        CtlgCatalogo c = manager.findObjectByParameter(CtlgCatalogo.class, map1);
        Map<String, Object> map = new HashMap<>();
        map.put("catalogo", c);
        return manager.findObjectByParameterList(CtlgItem.class, map);
    }

}
