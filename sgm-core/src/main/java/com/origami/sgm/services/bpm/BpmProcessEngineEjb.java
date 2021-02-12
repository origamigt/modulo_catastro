/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.bpm;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import org.activiti.engine.ProcessEngine;
import util.ApplicationContextUtils;

/**
 * IMplementacion de {@link BpmProcessEngine}. Service que permite obtener los
 * datos de configuracion para el api de activiti y conexion a la base.
 *
 * @author CarlosLoorVargas
 */
@Singleton(name = "bpmProcessEngine")
@Lock(LockType.READ)
public class BpmProcessEngineEjb implements BpmProcessEngine {

    private ProcessEngine processEngine;

    /**
     * Obtiene las propiedades de confiracion para la base activiti bpmn
     *
     */
    @PostConstruct
    private void init() {
        processEngine = (ProcessEngine) ApplicationContextUtils.getBean("processEngine");
        //processEngine.getProcessEngineConfiguration()
    }

    /**
     * Devuelve {@link ProcessEngine} con los datos de configuracion de
     * activiti.
     *
     * @return {@link ProcessEngine}
     */
    @Override
    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

}
