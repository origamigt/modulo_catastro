/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.origami.app.cdi.jpa.hibernate.HibernateFactory;
import com.origami.app.cdi.jpa.hibernate.UnitQualifier;
import java.lang.annotation.Annotation;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Permite inicializar el hibernate.
 *
 * @author User
 */
public class HibernateUtil {

    private static SessionFactory sessionFactoryGis;
    public static Configuration cfg;

    public static HibernateFactory getFactory() {
        UnitQualifier uq = new UnitQualifier() {
            @Override
            public String value() {
                return "sgm";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return UnitQualifier.class;
            }

        };
        return BeanProvider.getContextualReference(HibernateFactory.class, uq);
    }

    public static ThreadLocal<Boolean> yaIniciada = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false; //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static ThreadLocal<Boolean> rollbackOnly = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false; //To change body of generated methods, choose Tools | Templates.
        }
    };

    static {
        try {
            // Create the SessionFactory from standard (hibernate.cfg.xml) 
            // config file.
//            cfg = new Configuration().configure();

//            sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            // Log the exception. 
//            System.err.println("Initial SessionFactory creation failed." + ex);
//            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
//        if(sessionFactory==null){
//            initSession();
//        }
        return HibernateUtil.getFactory().getFactory();
    }

//    private static synchronized void initSession(){
//        if(sessionFactory!=null) return;
//        try {
//            if(SchemasConfig.DB_ENGINE == DatabaseEngine.POSTGRESQL){
//                initPostgresSession();
//            } else {
//                initOracleSession();
//            }
//        } catch (Throwable ex) {
//            // Log the exception. 
//            System.err.println("Initial SessionFactory creation failed." + ex);
//            throw new ExceptionInInitializerError(ex);
//        }
//    }
//    
//    private synchronized static void initOracleSession(){
//        cfg = new Configuration();
//        //cfg.addResource("oracle-hibernate.cfg.xml");
//        cfg.configure("oracle-hibernate.cfg.xml");
//        cfg.setNamingStrategy(OracleNamingStrategy.INSTANCE);
//        sessionFactory = cfg.buildSessionFactory();
//    }
    public static SessionFactory getSessionFactoryGis() {
        if (sessionFactoryGis == null) {
            initSessionGis();
        }
        return sessionFactoryGis;
    }
//    

    private synchronized static void initSessionGis() {
        cfg = new Configuration().configure("hibernate-gis.cfg.xml");
        sessionFactoryGis = cfg.buildSessionFactory();
    }

}
