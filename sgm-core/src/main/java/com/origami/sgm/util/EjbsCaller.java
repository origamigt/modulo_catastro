/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.util;

import com.origami.config.SisVars;
import com.origami.sgm.bpm.models.ProcessDef;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.HistoricoReporteTramite;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.services.bpm.BpmBaseEngine;
import com.origami.sgm.services.interfaces.SeqGenMan;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import utils.ejb.interfaces.DatabaseLocal;

/**
 * Utiliza el {@link BeanProvider} para obtener los ejb.
 *
 * @author CarlosLoorVargas
 */
public abstract class EjbsCaller {

    private static Entitymanager manager = null;
    private static BpmBaseEngine engine = null;
    private static DatabaseLocal dataSocurce = null;
    private static SeqGenMan sequences = null;

    public static Entitymanager getTransactionManager() {
        return BeanProvider.getContextualReference(Entitymanager.class);
//        try {
//            manager = (Entitymanager) new InitialContext().lookup(SisVars.entityManager);
//        } catch (Exception e) {
//            manager = null;
//            Logger.getLogger(EjbsCaller.class.getName()).log(Level.SEVERE, null, e);
//        }
//        return manager;
    }

    public static BpmBaseEngine getEngine() {
        return BeanProvider.getContextualReference(BpmBaseEngine.class);
//        try {
//            engine = (BpmBaseEngine) new InitialContext().lookup(SisVars.bpmBaseEngine);
//        } catch (Exception e) {
//            Logger.getLogger(EjbsCaller.class.getName()).log(Level.SEVERE, null, e);
//        }
//        return engine;
    }

    public static ProcessDef getProcessDef(String key) {
        ProcessDef pd = null;
        try {
            ProcessDefinition p = getEngine().getProcessDefinitionByKey(key);
            if (p != null) {
                pd = new ProcessDef();
                pd.setId(p.getId());
                pd.setKey(p.getKey());
                pd.setName(p.getName());
                pd.setDescription(p.getDescription());
                pd.setDeploymentId(p.getDeploymentId());
                pd.setVersion(p.getVersion());
                pd.setDiagramResourceName(p.getDiagramResourceName());
                pd.setResourceName(p.getResourceName());
                pd.setSuspended(p.isSuspended());
                pd.setHasStartFormKey(p.hasStartFormKey());
            }
        } catch (Exception e) {
            Logger.getLogger(EjbsCaller.class.getName()).log(Level.SEVERE, null, e);
        }
        return pd;
    }

    public static DataSource getDataSource() {
        try {
            dataSocurce = (DatabaseLocal) new InitialContext().lookup(SisVars.datasource);
        } catch (Exception e) {
            Logger.getLogger(EjbsCaller.class.getName()).log(Level.SEVERE, null, e);
        }
        return dataSocurce.getDataSource();
    }

    public static boolean getHistTramRep(Object id, String url) {
        boolean flag = false;
        try {
            HistoricoReporteTramite h = getTransactionManager().find(HistoricoReporteTramite.class, id);
            if (h != null) {
                h.setUrl(url);
                flag = getTransactionManager().persist(h) != null;
            }
        } catch (Exception e) {
            Logger.getLogger(EjbsCaller.class.getName()).log(Level.SEVERE, null, e);
        }
        return flag;
    }

    public static void updateHistoricoTramites(String processInstanceId) {
        HistoricoTramites ht = (HistoricoTramites) getTransactionManager().find(Querys.getHistoricProceduresByProcId, new String[]{"idprocess"}, new Object[]{processInstanceId});
        ht.setEstado("finalizado");
        getTransactionManager().update(ht);
    }

    public static SeqGenMan getSequences() {
        try {
            sequences = (SeqGenMan) new InitialContext().lookup("java:module/seqManager");
        } catch (Exception e) {
            Logger.getLogger(EjbsCaller.class.getName()).log(Level.SEVERE, null, e);
        }
        return sequences;
    }

}
