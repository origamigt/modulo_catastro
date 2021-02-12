/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.reportes;

import com.origami.app.AppConfig;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.FotoPredio;
import com.origami.sgm.entities.MsgFormatoNotificacion;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgm.util.EjbsCaller;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import util.HiberUtil;
import util.Utils;

/**
 * Con los parametro que recibe por el CDI {@link ServletSession} le pasa los
 * parametros al reporte y lo genera y lo envia mostrar en formato PDF.
 *
 * @author origami-idea
 */
@WebServlet(name = "FichaDownload", urlPatterns = {"/FichaDownload"})
public class DocumentoFichas extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(DocumentoFichas.class.getName());

    @Inject
    private ServletSession ss;
    @javax.inject.Inject
    protected Entitymanager manager;
    @Inject
    protected OmegaUploader omegaUploader;
    @javax.inject.Inject
    protected CatastroServices catas;
    @Inject
    protected AppConfig appconfig;
    private Connection conn = null;

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
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=Fichas_" + new Date().getTime() + ".zip");

        String rutaArchivos = "/servers_files/Fichas/";

        List<CatPredio> getListPredios = null;
        try {
            String codigoMz = (String) ss.getParametros().get("codigoMz");
            if (codigoMz != null) {
                if (codigoMz.length() > 0) {
                    getListPredios = manager.findAllEntCopy(String.format(Querys.getFichaCatastralMz, ("'%" + codigoMz + "%'")));
                } else {
                    getListPredios = manager.findAllEntCopy(Querys.getFichaCatastral);
                }
            } else {
                getListPredios = manager.findAllEntCopy(Querys.getFichaCatastral);
            }
            CatPredio x1 = getListPredios.get(0);
            Short mzAux = x1.getMz();
            String rutaRepo = "/servers_files/Fichas/Parroquia " + x1.getParroquia().toString() + "/SECTOR " + x1.getSector().toString() + "/" + (x1.getSector().toString() + "_MZ_" + Utils.completarCadenaConCeros(mzAux + "", 3));
            rutaArchivos = "/servers_files/Fichas/Parroquia " + x1.getParroquia().toString() + "/SECTOR " + x1.getSector().toString();
            String slash = "/";
            Path path = Paths.get(rutaRepo.endsWith("/") ? rutaRepo.concat("/") : rutaRepo);
            Files.createDirectories(path);

            Session sess = HiberUtil.getSession();
            SessionImplementor sessImpl = (SessionImplementor) sess;
            conn = sessImpl.getJdbcConnectionAccess().obtainConnection();
            int count = 1;
            for (CatPredio pre : getListPredios) {
                if (mzAux != pre.getMz()) {
                    mzAux = pre.getMz();
                    rutaRepo = "/servers_files/Fichas/Parroquia " + x1.getParroquia().toString() + "/SECTOR " + x1.getSector().toString() + "/" + (x1.getSector().toString() + "_MZ_" + Utils.completarCadenaConCeros(mzAux + "", 3));
                    path = Paths.get(rutaRepo.endsWith("/") ? rutaRepo.concat("/") : rutaRepo);
                    Files.createDirectories(path);
                }
                System.out.println("Generando " + count + "de " + getListPredios.size() + " ficha: " + Utils.completarCadenaConCeros(pre.getSolar() + "", 3) + " ");
                if (!path.toFile().exists()) {
                    if (path.toFile().mkdir()) {
                        System.out.println("Creado Directorio Correctamente " + path.toFile().getAbsolutePath());
                    }
                }
                imprimirFicha(pre, response, path.toFile().toString() + slash);
                count++;
            }
            try {
                ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
                zipDir(rutaArchivos, zos);
                //close the stream
                zos.close();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, null, e);
            }
            if (ss.getReportes() != null) {
                ss.getReportes().clear();
            }
            ss.setAgregarReporte(Boolean.FALSE);
            ss.borrarDatos();

        } catch (SQLException | IOException e) {
            printerError(response, "Ocurrio un error al generar las fichas");
            if (ss.getReportes() != null) {
                ss.getReportes().clear();
            }
            ss.setAgregarReporte(Boolean.FALSE);
            LOG.log(Level.SEVERE, null, e);
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
            LOG.log(Level.SEVERE, null, ex);
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
//            LOG.log(Level.SEVERE, null, ex);
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

    public void zipDir(String dir2zip, ZipOutputStream zos) {
        try {
            //create a new File object based on the directory we have to zip File
            File zipDir = new File(dir2zip);
            //get a listing of the directory content
            String[] dirList = zipDir.list();
            byte[] readBuffer = new byte[2156];
            int bytesIn = 0;
            //loop through dirList, and zip the files
            for (int i = 0; i < dirList.length; i++) {
                File f = new File(zipDir, dirList[i]);
                if (f.isDirectory()) {
                    //if the File object is a directory, call this
                    //function again to add its content recursively
                    String filePath = f.getPath();
                    zipDir(filePath, zos);
                    //loop again
                    continue;
                }
                //if we reached here, the File object f was not a directory
                //create a FileInputStream on top of f
                FileInputStream fis = new FileInputStream(f);
//            create a new zip entry
                ZipEntry anEntry = new ZipEntry(f.getPath());
                //place the zip entry in the ZipOutputStream object
                zos.putNextEntry(anEntry);
                //now write the content of the file to the ZipOutputStream
                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }
                //close the Stream
                fis.close();
            }
        } catch (Exception e) {
            //handle exception
        }
    }

    private void imprimirFicha(CatPredio p, HttpServletResponse response, String ruta) {
        try {
            if (p != null) {
                try {
                    if (p != null) {
                        ss.instanciarParametros();
                        int numFotos = 1;
                        List<FotoPredio> fotos = null;
                        if (p.getPredioRaiz() == null) {
                            fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{p.getId()});
                        } else {
                            fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{p.getPredioRaiz().longValue()});
                        }
                        if (Utils.isNotEmpty(fotos)) {
                            for (FotoPredio foto : fotos) {
                                switch (numFotos) {
                                    case 1:
                                        ss.agregarParametro("FachadaFrontal", omegaUploader.streamFile(foto.getFileOid()));
                                        break;
                                    case 2:
                                        ss.agregarParametro("FachadaIzquierda", omegaUploader.streamFile(foto.getFileOid()));
                                        break;
                                    case 3:
                                        ss.agregarParametro("FachadaDerecha", omegaUploader.streamFile(foto.getFileOid()));
                                        break;
                                    case 4:
                                        ss.agregarParametro("FachadaPosterior", omegaUploader.streamFile(foto.getFileOid()));
                                        break;
                                }
                                numFotos++;
                            }
                        }

                        ss.agregarParametro("IMAGEN_PREDIO", appconfig.formatUrlPredio(p.getClaveCat(), p.getPropiedadHorizontal()));
                        ss.agregarParametro("COLINDANTES", appconfig.formatUrlPredioColindates(p.getClaveCat()));
                        ss.agregarParametro("MUNICIPIO", SisVars.NOMBREMUNICIPIO);
                        ss.agregarParametro("predio", p.getId());
                        ss.agregarParametro("LOGO", getServletContext().getRealPath(SisVars.sisLogo));
                        ss.agregarParametro("LOGO2", getServletContext().getRealPath(SisVars.sisLogo1));
                        ss.setNombreReporte("Ficha predial " + p.getNumPredio());
                        ss.agregarParametro("SUBREPORT_DIR", getServletContext().getRealPath("reportes/catastro/Ibarra/"));
                        ss.agregarParametro("SUBREPORT_DIR_TITLE", getServletContext().getRealPath("reportes/"));
                        ss.setAgregarReporte(true);
                        // Parametros para agregar un reporte al final del primero
                        Map<String, Object> reporteadd = new HashMap<>();
                        ss.setAgregarReporte(Boolean.FALSE);
                        Collection<CatPredioEdificacion> bloques = catas.getEdificaciones(p);
                        if (bloques != null && Utils.isNotEmpty(bloques)) {
                            if (ss.getReportes() != null) {
                                ss.getReportes().clear();
                            }
                            int count = 0;
                            String edificaciones = "";
                            ss.setAgregarReporte(Boolean.TRUE);
                            for (CatPredioEdificacion edif1 : bloques) {
                                count++;
                                edificaciones += edif1.getId().toString() + ",";
                                if ((count % 4) == 0 || (bloques.size() - count) == 0) {
                                    edificaciones = edificaciones.substring(0, edificaciones.length() - 1);
                                    reporteadd = new HashMap<>();
                                    reporteadd.put("nombreSubCarpeta", "catastro/Ibarra/");
                                    reporteadd.put("nombreReporte", "fichaMiduviBloques");
                                    reporteadd.put("predio", p.getId());
                                    reporteadd.put("edificaciones", edificaciones);
                                    ss.addParametrosReportes(reporteadd);
                                    edificaciones = "";
                                }
                            }
                        }
                        ss.setNombreSubCarpeta("catastro/Ibarra");
                        ss.setNombreReporte("fichaMiduvi");
                        ss.setNombreDocumento(ruta + "MZ_" + Utils.completarCadenaConCeros(p.getMz() + "", 3) + "_" + Utils.completarCadenaConCeros(p.getSolar() + "", 3) + ".pdf");
                        ss.setTieneDatasource(Boolean.TRUE);
                        generarPdf(getServletContext().getRealPath("/reportes/catastro/Ibarra/fichaMiduvi.jasper"), ss.getParametros());
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, null, e);
                }
                System.out.println("Ruta " + ruta);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    public Boolean generarPdf(String nombre, Map paramt) throws SQLException {
        JasperPrint jasperPrint = null;
        try {
            if (conn == null) {
                Session sess = HiberUtil.getSession();
                SessionImplementor sessImpl = (SessionImplementor) sess;
                conn = sessImpl.getJdbcConnectionAccess().obtainConnection();
            }
            System.out.println("Ejecutando Reporte >> " + nombre + (" Sub reportes " + (ss.getReportes() == null ? 0 : ss.getReportes().size())));
            jasperPrint = JasperFillManager.fillReport(nombre, paramt, conn);
            if (ss.getAgregarReporte() != null && ss.getAgregarReporte()) {
                for (Map reporte : ss.getReportes()) {
                    if (conn == null) {
                        Session sess = HiberUtil.getSession();
                        SessionImplementor sessImpl = (SessionImplementor) sess;
                        conn = sessImpl.getJdbcConnectionAccess().obtainConnection();
                    }
                    String rp = null;
                    if (reporte.containsKey("nombreSubCarpeta")) {
                        String subRp = "/reportes/" + reporte.get("nombreSubCarpeta") + "/" + reporte.get("nombreReporte") + ".jasper";
                        rp = getServletContext().getRealPath(subRp);
                    } else {
                        String subRp = "/reportes/" + ss.getNombreSubCarpeta() + "/" + reporte.get("nombreReporte") + ".jasper";
                        rp = getServletContext().getRealPath(subRp);
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
            JasperExportManager.exportReportToPdfFile(jasperPrint, ss.getNombreDocumento());

        } catch (JRException | SQLException e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return true;
    }
}
