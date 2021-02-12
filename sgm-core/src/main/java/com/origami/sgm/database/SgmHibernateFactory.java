package com.origami.sgm.database;

import com.origami.app.cdi.jpa.hibernate.HibernateAddClassesEvent;
import com.origami.app.cdi.jpa.hibernate.HibernateFactory;
import com.origami.app.cdi.jpa.hibernate.UnitQualifier;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 *
 * @author Fernando
 */
@ApplicationScoped
@UnitQualifier("sgm")
public class SgmHibernateFactory extends HibernateFactory {

    @Inject
    @UnitQualifier("sgm")
    protected Event<HibernateAddClassesEvent> acEvent;

    @Override
    protected void fireAddClassesEvent() {
        acEvent.fire(new HibernateAddClassesEvent());
    }

    @Override
    protected String getHibernateCfgXml() {
        if (SchemasConfig.DB_ENGINE == DatabaseEngine.POSTGRESQL) {
            return super.getHibernateCfgXml();
        } else {
            return "oracle-hibernate.cfg.xml";
        }
    }

    @Override
    protected void configNamingStrategy() {
        if (SchemasConfig.DB_ENGINE == DatabaseEngine.ORACLE) {
            cfg.setNamingStrategy(OracleNamingStrategy.INSTANCE);
        }
    }

}
