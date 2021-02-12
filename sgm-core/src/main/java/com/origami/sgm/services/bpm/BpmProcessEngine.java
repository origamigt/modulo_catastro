/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.bpm;

import javax.ejb.Local;
import org.activiti.engine.ProcessEngine;

/**
 * Service que permite obtener los datos de configuracion para el api de
 * activiti y conexion a la base.
 *
 * @author Origami Integrales
 */
@Local
public interface BpmProcessEngine {

    public ProcessEngine getProcessEngine();

}
