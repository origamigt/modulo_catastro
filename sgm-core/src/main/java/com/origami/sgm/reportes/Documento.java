/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.reportes;

import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.entities.MsgFormatoNotificacion;
import com.origami.sgm.util.EjbsCaller;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import util.HiberUtil;

/**
 * Con los parametro que recibe por el CDI {@link ServletSession} le pasa los
 * parametros al reporte y lo genera y lo envia mostrar en formato PDF.
 *
 * @author origami-idea
 */
@WebServlet(name = "Documento", urlPatterns = {"/Documento"})
public class Documento extends HttpServlet {

    @Inject
    private ServletSession servletSession;
    @Inject
    private UserSession userSession;

    private Map parametros;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws java.sql.SQLException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        JasperPrint jasperPrint;
        OutputStream outStream;
        Connection conn = null;
        if (servletSession.estaVacio()) {
            this.printerError(response, "No hay datos que mostrar");
            return;
        }
        parametros = servletSession.getParametros();

        parametros.put("LOGO2", getServletContext().getRealPath(SisVars.sisLogo1));
        parametros.put("LOGO", getServletContext().getRealPath(SisVars.sisLogo));
        parametros.putIfAbsent("SUBREPORT_DIR", getServletContext().getRealPath("//reportes//"));
        parametros.putIfAbsent("SUBREPORT_DIR_TITLE", getServletContext().getRealPath("//reportes//"));
        parametros.putIfAbsent("NOMBRE_USUARIO", userSession.getNombrePersonaLogeada());
//        System.out.println("Parametros reporte: " + parametros);

        try {
            request.setCharacterEncoding("UTF-8");
//            InputStream ruta;
            String ruta;
            System.out.println("Nombre reporte: " + servletSession.getNombreReporte());
            if (servletSession.getNombreSubCarpeta() == null) {
                ruta = getServletContext().getRealPath("//reportes//" + servletSession.getNombreReporte() + ".jasper");
            } else {
                ruta = getServletContext().getRealPath("//reportes//" + servletSession.getNombreSubCarpeta() + "//" + servletSession.getNombreReporte() + ".jasper");
            }

            if (ruta == null) {
                System.out.println("Ruta reporte " + (servletSession.getNombreSubCarpeta() == null ? "" : servletSession.getNombreSubCarpeta().concat("/")) + servletSession.getNombreReporte());
                this.printerError(response, "No se encontro el reporte " + (servletSession.getNombreSubCarpeta() == null ? "" : servletSession.getNombreSubCarpeta().concat("/")) + servletSession.getNombreReporte());
                return;
            }
            System.out.println("Ruta " + ruta);
            response.setContentType("application/pdf");
            if (servletSession.tieneParametro("ciRuc")) {
                response.addHeader("Content-disposition", "filename=" + servletSession.getNombreReporte() + servletSession.retornarValor("ciRuc") + ".pdf");
            } else {
                response.addHeader("Content-disposition", "filename=" + servletSession.getNombreReporte() + "(" + new Date().getTime() + ")" + ".pdf");
            }
            Session sess = HiberUtil.getSession();
            SessionImplementor sessImpl = (SessionImplementor) sess;
            conn = sessImpl.getJdbcConnectionAccess().obtainConnection();
            Logger.getLogger(Documento.class.getName()).log(Level.INFO, "Generando Reporte >> {0}", servletSession.getNombreReporte());
            if (servletSession.getTieneDatasource()) {

//                Utils.reemplazarRutaSubReportes(parametros);
                jasperPrint = JasperFillManager.fillReport(ruta, parametros, conn);
                if (servletSession.getEncuadernacion() != null && servletSession.getEncuadernacion()) {
                    List pages = jasperPrint.getPages();
                    JRPrintPage page;
                    List<JRPrintElement> elements;
                    for (int i = 1; i < pages.size() + 1; i++) {
                        page = (JRPrintPage) pages.get(i - 1);
                        elements = page.getElements();
                        if (i % 2 != 0) {//IMPAR
                            for (JRPrintElement temp : elements) {
                                temp.setX(temp.getX() + 30);
                            }
                        } else {//PAR
                            for (JRPrintElement temp : elements) {
                                temp.setX(temp.getX() - 30);
                            }
                        }
                    }
                }
            } else {
                parametros.putIfAbsent("REPORT_CONNECTION", conn);
//                Utils.reemplazarRutaSubReportes(parametros);
                System.out.println("Reportes " + parametros);
                JRDataSource dataSource = new JRBeanCollectionDataSource(new ArrayList<Object>());
                if (servletSession.getDataSource() != null) {
                    dataSource = new JRBeanCollectionDataSource(servletSession.getDataSource());
                }
                System.out.println("Ejecutando reporte...");
                jasperPrint = JasperFillManager.fillReport(ruta, parametros, dataSource);
            }
            if (servletSession.getAgregarReporte() != null && servletSession.getAgregarReporte()) {
                if (servletSession.getReportes() != null || servletSession.getReportes().isEmpty()) {
                    for (Map reporte : servletSession.getReportes()) {
                        System.out.println("Agregando Reporte >>" + reporte.get("nombreReporte"));
                        if (reporte.containsKey("nombreSubCarpeta")) {
//                            ruta = Documento.class.getResourceAsStream("/reportes/" + reporte.get("nombreSubCarpeta") + "/" + reporte.get("nombreReporte") + ".jasper");
                            ruta = getServletContext().getRealPath("/reportes/" + reporte.get("nombreSubCarpeta") + "/" + reporte.get("nombreReporte") + ".jasper");
                        } else {
//                            ruta = Documento.class.getResourceAsStream("/reportes/" + servletSession.getNombreSubCarpeta() + "/" + reporte.get("nombreReporte") + ".jasper");
                            ruta = getServletContext().getRealPath("/reportes/" + servletSession.getNombreSubCarpeta() + "/" + reporte.get("nombreReporte") + ".jasper");
                        }
//                        Utils.reemplazarRutaSubReportes(reporte);
                        System.out.println("Reportes " + reporte);
                        JasperPrint jasperPrint2 = null;
                        if (reporte.containsKey("DATASOURCE")) {
                            JRDataSource dataSource = new JRBeanCollectionDataSource((Collection<?>) reporte.get("DATASOURCE"));
                            jasperPrint2 = JasperFillManager.fillReport(ruta, reporte, dataSource);
                        } else {
                            if (conn == null) {
                                sess = HiberUtil.getSession();
                                sessImpl = (SessionImplementor) sess;
                                conn = sessImpl.getJdbcConnectionAccess().obtainConnection();
                            }
                            jasperPrint2 = JasperFillManager.fillReport(ruta, reporte, conn);
                        }
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
            }
            outStream = response.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outStream);
//            ruta.close();
            outStream.flush();
            outStream.close();
            if (servletSession.getReportes() != null) {
                servletSession.getReportes().clear();
            }
            servletSession.setAgregarReporte(Boolean.FALSE);
            servletSession.borrarDatos();

        } catch (SQLException | JRException | IOException e) {
            if (servletSession.getReportes() != null) {
                servletSession.getReportes().clear();
            }
            servletSession.setAgregarReporte(Boolean.FALSE);
            Logger.getLogger(Documento.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(Documento.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        try {
//            processRequest(request, response);
//        } catch (SQLException ex) {
//            Logger.getLogger(Documento.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void printerError(HttpServletResponse response, String mensaje) throws IOException {
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        MsgFormatoNotificacion msg = EjbsCaller.getTransactionManager().find(MsgFormatoNotificacion.class, new Long(1));
        if (msg != null) {
            pw.println(msg.getHeader());
            pw.println("<center><p><h1>" + mensaje + "</h1></p></center>");
            pw.println(msg.getFooter());
        }
    }
}
