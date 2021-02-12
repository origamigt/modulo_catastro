package com.origami.sgm.acl.service;

import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgm.services.ejbs.SgmEEService;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;

@Singleton
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
@ApplicationScoped
public class AclRolDom extends SgmEEService {

}
