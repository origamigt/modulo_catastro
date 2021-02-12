/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.report.servlet;

import com.origami.report.cdi.MimeTypes;
import com.origami.report.cdi.ServletSessionReport;
import com.origami.report.cdi.Utils;
import com.origami.report.model.Variables;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;

/**
 *
 * @author ANGEL NAVARRO
 */
@WebServlet(name = "exporter", urlPatterns = {"/exporter/xlsx"}, largeIcon = "#{appConfig.urlbase}resources/images/favicon.ico",
        smallIcon = "#{appConfig.urlbase}resources/images/favicon.ico")
public class ExporterReport extends HttpServlet {

    @Inject
    private ServletSessionReport servletSession;
    private String name = "hoja";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        OutputStream outStream;

        try {
            request.setCharacterEncoding("UTF-8");
            response.setContentType(MimeTypes.getMimeTypeForExtension("xlsx"));
            response.addHeader("Content-disposition", "filename=Informe(" + new Date().getTime() + ").xlsx");
            outStream = response.getOutputStream();
            // WRITE FILE
//            processFileXlsx(outStream);
            processWorkBookStreaming(outStream);
            outStream.flush();
            outStream.close();
            if (servletSession.getReportes() != null) {
                servletSession.getReportes().clear();
            }
            servletSession.setAgregarReporte(Boolean.FALSE);
            servletSession.borrarDatos();

        } catch (IOException e) {
            if (servletSession.getReportes() != null) {
                servletSession.getReportes().clear();
            }
            servletSession.setAgregarReporte(Boolean.FALSE);
            Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, e);
            this.printerError(response, e.getMessage());
        }
    }

    private void processFileXlsx(OutputStream outputStream) {
        PreparedStatement statement = null;
        ResultSet result = null;
        Connection conn = null;
        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(servletSession.getNombreReporte());
            sheet.setAutobreaks(true);
//            sheet.setr
            // Obtenemos la conexion a la base de datos
            Session sess = Utils.getSession("util.HiberUtil");
            SessionImplementor sessImpl = (SessionImplementor) sess;
            conn = sessImpl.getJdbcConnectionAccess().obtainConnection();
            String sentencia = (String) servletSession.getParametro(Variables.SENTENCIA);
            // Realizamos la consulta a la base de datos
            statement = conn.prepareStatement(sentencia);
            // Ejecutamos la sentencia sql
            result = statement.executeQuery();
            //Recorremos las columnas del resultSet
            int rowNum = 0;
            int countColumns = result.getMetaData().getColumnCount();
            ResultSetMetaData metaDataColumns = result.getMetaData();
            // Creamos un nuevo ROW para el header
            XSSFRow header = sheet.createRow(rowNum);
            System.out.println("Add Header");
            for (int colHeader = 1; colHeader <= countColumns; colHeader++) {
                // Creamos una nueva celda para cada una de las celdas
                XSSFCell cellHeader = header.createCell(colHeader - 1);
                // agregamos el valor de la celda
                cellHeader.setCellValue(metaDataColumns.getColumnName(colHeader).toUpperCase());
            }
            rowNum++;
            // Verificamos si hay datos
            System.out.println("Add Row Data");
            while (result.next()) {
                // Creamos un nuevo ROW para los cada nueva fila del resultSet
                XSSFRow data = sheet.createRow(rowNum);
                // Recorremos los datos de las columnas
                for (int rowdata = 1; rowdata <= countColumns; rowdata++) {
                    // Creamos una nueva celda para cada una de las celdas
                    XSSFCell cellData = data.createCell(rowdata - 1);
                    // agregamos el valor de la celda
                    Object object = result.getObject(rowdata);
                    if (object == null) {
                        cellData.setCellValue("");
                    } else {
                        switch (metaDataColumns.getColumnType(rowdata)) {
                            case Types.BOOLEAN:
                                cellData.setCellValue((boolean) object);
                                break;
                            case Types.DATE:
                                cellData.setCellValue((Date) object);
                            case Types.TIMESTAMP_WITH_TIMEZONE:
                                cellData.setCellValue((Date) object);
                                break;
                            case Types.NUMERIC:
                                cellData.setCellValue(((BigDecimal) object).doubleValue());
                                break;
                            case Types.FLOAT:
                                cellData.setCellValue(((Float) object).doubleValue());
                                break;
                            case Types.INTEGER:
                                cellData.setCellValue(((Integer) object).doubleValue());
                                break;
                            case Types.SMALLINT:
                                cellData.setCellValue(((Integer) object).doubleValue());
                                break;
                            case Types.BIGINT:
                                cellData.setCellValue(((Long) object).doubleValue());
                                break;
                            default:
                                cellData.setCellValue(object + "");
                                break;
                        }
                    }
                }
                rowNum++;
                if ((rowNum % 2000) == 0) {
                    System.out.println("Procesando " + rowNum);
                }
            }
            sheet.setAutoFilter(new CellRangeAddress(0, rowNum, 0, countColumns - 1));
            sheet.setAutobreaks(true);
            sheet.autoSizeColumn(0);
            sheet.setFitToPage(true);
        } catch (SQLException ex) {
            Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
                statement.close();
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                workbook.write(outputStream);
                System.out.println("Write Data...");
                workbook.close();
            } catch (FileNotFoundException e) {
                Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, e);
            } catch (IOException e) {
                Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, e);
            }
            System.out.println("Done");
        }
    }

    private void processWorkBookStreaming(OutputStream outputStream) {
        PreparedStatement statement = null;
        ResultSet result = null;
        Connection conn = null;
        org.apache.poi.xssf.streaming.SXSSFWorkbook workbook = null;
        try {

            // Obtenemos la conexion a la base de datos
            Session sess = Utils.getSession("util.HiberUtil");
            SessionImplementor sessImpl = (SessionImplementor) sess;
            conn = sessImpl.getJdbcConnectionAccess().obtainConnection();
            conn.setAutoCommit(false);
            String sentencia = (String) servletSession.getParametro(Variables.SENTENCIA);
            // Realizamos la consulta a la base de datos
            statement = conn.prepareStatement(sentencia);
            // Ejecutamos la sentencia sql
            result = statement.executeQuery();
            //Recorremos las columnas del resultSet
            int rowNum = 0;
            int countColumns = result.getMetaData().getColumnCount();
            // Creamos el libro para almacenar la data
            workbook = new SXSSFWorkbook(100);
            SXSSFSheet sheet = workbook.createSheet(name);
            createNewSheet(sheet, rowNum, countColumns, result, workbook);
        } catch (SQLException ex) {
            Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
                statement.close();
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                workbook.write(outputStream);
                System.out.println("Write Data...");
                workbook.close();
            } catch (FileNotFoundException e) {
                Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, e);
            } catch (IOException e) {
                Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, e);
            }
            System.out.println("Done");
        }
    }

    private void createNewSheet(SXSSFSheet sheet, int rowNum, int countColumns, ResultSet result, SXSSFWorkbook workbook) throws SQLException {
        // obtenemos todas la columnas que hay en el resulset
        ResultSetMetaData metaDataColumns = result.getMetaData();
        sheet.setAutobreaks(true);
        sheet.setAutoFilter(new CellRangeAddress(0, rowNum, 0, countColumns - 1));
//            sheet.autoSizeColumn(0);
        sheet.setFitToPage(true);
        // Creamos un nuevo ROW para el header
        SXSSFRow header = sheet.createRow(rowNum);
        System.out.println("Add Header");
        for (int colHeader = 1; colHeader <= countColumns; colHeader++) {
            // Creamos una nueva celda para cada una de las celdas
            SXSSFCell cellHeader = header.createCell(colHeader - 1);
            // agregamos el valor de la celda
            cellHeader.setCellValue(metaDataColumns.getColumnName(colHeader).toUpperCase());
        }
        rowNum++;
        // Verificamos si hay datos
        System.out.println("Add Row Data");
        while (result.next()) {
            // Creamos un nuevo ROW para los cada nueva fila del resultSet
            SXSSFRow data = sheet.createRow(rowNum);
            // Recorremos los datos de las columnas
            for (int rowdata = 1; rowdata <= countColumns; rowdata++) {
                // Creamos una nueva celda para cada una de las celdas
                SXSSFCell cellData = data.createCell(rowdata - 1);
                // agregamos el valor de la celda
                Object object = result.getObject(rowdata);
                if (object == null) {
                    cellData.setCellValue("");
                } else {
                    switch (metaDataColumns.getColumnType(rowdata)) {
                        case Types.BOOLEAN:
                            cellData.setCellValue((boolean) object);
                            break;
                        case Types.DATE:
                            cellData.setCellValue((Date) object);
                        case Types.TIMESTAMP_WITH_TIMEZONE:
                            cellData.setCellValue((Date) object);
                            break;
                        case Types.NUMERIC:
                            cellData.setCellValue(((BigDecimal) object).doubleValue());
                            break;
                        case Types.FLOAT:
                            cellData.setCellValue(((Float) object).doubleValue());
                            break;
                        case Types.INTEGER:
                            cellData.setCellValue(((Integer) object).doubleValue());
                            break;
                        case Types.SMALLINT:
                            cellData.setCellValue(((Integer) object).doubleValue());
                            break;
                        case Types.BIGINT:
                            cellData.setCellValue(((Long) object).doubleValue());
                            break;
                        default:
                            cellData.setCellValue(object + "");
                            break;
                    }
                }
            }
            // Incrementamos el contador de registros
            rowNum++;
            // Imprimimos cada 10000 registros procesados
            if ((rowNum % 10000) == 0) {
                System.out.println("Procesando " + rowNum);
            }
            // Validamos el maximo de registros que soporta excel 2007
            // Creamos una nueva hoja para los siguinetes registros
            if ((rowNum % 1048570) == 0) {
                // creamos una nueva hoja
                sheet = workbook.createSheet(name + (workbook.getNumberOfSheets() + 1));
                // enviamos a llenar la hoja
                createNewSheet(sheet, 0, countColumns, result, workbook);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printerError(HttpServletResponse response, String mensaje) throws IOException {
        try {
            response.setContentType("text/html");
            PrintWriter pw = response.getWriter();
//            MsgFormatoNotificacion msg = EjbsCaller.getTransactionManager().find(MsgFormatoNotificacion.class, new Long(1));
//            if (msg != null) {
            pw.println("");
            pw.println("<center><p><h1>" + mensaje + "</h1></p></center>");
            pw.println("");
//            }
        } catch (IOException ex) {
            Logger.getLogger(ExporterReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
