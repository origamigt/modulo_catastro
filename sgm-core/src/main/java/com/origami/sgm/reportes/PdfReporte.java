/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.reportes;

import com.origami.session.ServletSession;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import util.HiberUtil;
import util.JsfUti;

/**
 * Esta clase contiene metodos que reciben los parametros y el nombre del
 * reporte le y con eso genera el reporte y lo devuele con un arreglo de
 * {@link byte}
 *
 * @author supergold
 */
public class PdfReporte implements Serializable {

    public static final Long serialVersionUID = 1L;
    private String ruta;
    private Boolean agregarReporte = false;
    ServletSession servletSession;
    JasperPrint jasperPrint;

    JasperPrint reporte_view;

    public byte[] generarPdf(String nombre, Map paramt, ServletSession servletSession) throws SQLException {
        this.servletSession = servletSession;
        byte[] pdfByte = null;
        Connection conn = null;
        try {
            Session sess = HiberUtil.getSession();
            SessionImplementor implementor = (SessionImplementor) sess;
            conn = implementor.getJdbcConnectionAccess().obtainConnection();
            System.out.println("Ejecutando Reporte >> " + nombre + (" Sub reportes " + (servletSession.getReportes() == null ? 0 : servletSession.getReportes().size())));
            jasperPrint = JasperFillManager.fillReport(nombre, paramt, conn);
            if (servletSession.getAgregarReporte() != null && servletSession.getAgregarReporte()) {
                for (Map reporte : servletSession.getReportes()) {
                    if (conn == null) {
                        sess = HiberUtil.getSession();
                        SessionImplementor sessImpl = (SessionImplementor) sess;
                        conn = sessImpl.getJdbcConnectionAccess().obtainConnection();
                    }
                    String rp = null;
                    if (reporte.containsKey("nombreSubCarpeta")) {
                        String subRp = "/reportes/" + reporte.get("nombreSubCarpeta") + "/" + reporte.get("nombreReporte") + ".jasper";
//                        System.out.println("    >> Buscando Reporte >> " + subRp);
                        rp = JsfUti.getRealPath(subRp);
                    } else {
                        String subRp = "/reportes/" + servletSession.getNombreSubCarpeta() + "/" + reporte.get("nombreReporte") + ".jasper";
//                        System.out.println("    >> Buscando Reporte >> " + subRp);
                        rp = JsfUti.getRealPath(subRp);
                    }
                    System.out.println("    >> Agregando Reporte >> " + rp);
                    JasperPrint jasperPrint2 = JasperFillManager.fillReport(rp, reporte, conn);
                    if (jasperPrint2.getPages() != null && jasperPrint2.getPages().size() > 0) {
                        if (jasperPrint2.getPages().size() > 1) {
                            for (JRPrintPage page : jasperPrint2.getPages()) {
                                jasperPrint.addPage(page);
                            }
                        } else {
                            jasperPrint.addPage(jasperPrint2.getPages().get(0));
                        }
                    }
                }
            }
            pdfByte = JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (JRException | SQLException e) {
            Logger.getLogger(PdfReporte.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return pdfByte;
    }

    public JasperPrint getReporte_view() {
        return reporte_view;
    }

    public void setReporte_view(JasperPrint reporte_view) {
        this.reporte_view = reporte_view;
    }

    public ServletSession getServletSession() {
        return servletSession;
    }

    public void setServletSession(ServletSession servletSession) {
        this.servletSession = servletSession;
    }

}
