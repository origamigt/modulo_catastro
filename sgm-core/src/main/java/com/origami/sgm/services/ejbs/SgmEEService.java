package com.origami.sgm.services.ejbs;

import org.hibernate.Session;
import org.hibernate.Transaction;

import util.HiberUtil;

/**
 * Clase abstracta donde se obtiene la session de hibernate y ademas se obtiene
 * una nueva transaccion
 *
 * @author Angel Navarro
 */
public abstract class SgmEEService {

    protected Session sess() {
        return HiberUtil.getSession();
    }

    protected Transaction requireTx() {
        return HiberUtil.requireTransaction();
    }

}
