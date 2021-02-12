package org.geoapi.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.geoapi.config.ApplicationProperties;
import org.geoapi.services.PrediosService;
import org.geoapi.utils.WmsImageUtils;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.spatial.BBOX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/predio")
public class PredioResource {

    private static final Logger LOG = Logger.getLogger(PredioResource.class.getName());

    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private PrediosService prediosService;

    @RequestMapping(value = "/codigo/{codigo}/{esPh}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable String codigo, @PathVariable Boolean esPh) throws IOException {
        BBOX bbx = prediosService.predioBboxEnvelope(codigo);
        if (bbx == null) {
            System.out.println("Bounding Box null for  " + codigo);
            return null;
        }
        Integer width = 768;
        StringBuilder imageUrl = new StringBuilder(properties.getWmsUrl());
        imageUrl.append("?service=WMS")
                .append("&version=1.1.0")
                .append("&&request=GetMap")
                .append("&layers=").append(esPh ? properties.getCroquisLayerPh() : properties.getCroquisLayer())
                .append("&bbox=").append(bbx.getMinX()).append(",").append(bbx.getMinY()).append(",").append(bbx.getMaxX()).append(",").append(bbx.getMaxY())
                .append("&width=").append(width)
                .append("&height=").append(WmsImageUtils.heightCalculate(bbx, width).toString())
                .append("&srs=").append(properties.getSrid())
                .append("&format=image/png")
                .append("&env=").append(properties.getEnvFilter()).append(":").append(codigo);
        try {
            System.out.println("Execute url get image " + imageUrl.toString());
            URL url = new URL(imageUrl.toString());
            ResponseEntity<byte[]> responseEntity;
            InputStream is = url.openStream();
            HttpHeaders headers = new HttpHeaders();
            byte[] media = IOUtils.toByteArray(is);
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            responseEntity = new ResponseEntity<>(media, headers, org.springframework.http.HttpStatus.OK);
            return responseEntity;
        } catch (IOException iOException) {
            LOG.log(Level.SEVERE, codigo, iOException);
            return null;
        }
    }

    @RequestMapping(value = "/codigo/{codigo}/{width}/{esPh}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable String codigo, @PathVariable(required = false) Integer width, @PathVariable Boolean esPh) throws IOException {
        BBOX bbx = prediosService.predioBboxEnvelope(codigo);
        if (bbx == null) {
            System.out.println("Bounding Box null for  " + codigo);
            return null;
        }
        if (width == null) {
            width = 768;
        }
        StringBuilder imageUrl = new StringBuilder(properties.getWmsUrl());
        imageUrl.append("?service=WMS")
                .append("&version=").append(properties.getWmsVersion())
                .append("&&request=GetMap")
                .append("&layers=").append(esPh ? properties.getCroquisLayerPh() : properties.getCroquisLayer())
                .append("&bbox=").append(bbx.getMinX()).append(",").append(bbx.getMinY()).append(",").append(bbx.getMaxX()).append(",").append(bbx.getMaxY())
                .append("&width=").append(width)
                .append("&height=").append(WmsImageUtils.heightCalculate(bbx, width).toString())
                .append("&srs=").append(properties.getSrid())
                .append("&format=image/png")
                .append("&env=").append(properties.getEnvFilter()).append(":").append(codigo);
        try {
            System.out.println("Execute url get image " + imageUrl.toString());
            URL url = new URL(imageUrl.toString());
            ResponseEntity<byte[]> responseEntity;
            InputStream is = url.openStream();
            HttpHeaders headers = new HttpHeaders();
            byte[] media = IOUtils.toByteArray(is);
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            responseEntity = new ResponseEntity<>(media, headers, org.springframework.http.HttpStatus.OK);
            return responseEntity;
        } catch (IOException iOException) {
            LOG.log(Level.SEVERE, codigo, iOException);
            return null;
        }
    }

    @RequestMapping(value = "/codigo/colindantes/{codigo}/{width}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImageColindantes(@PathVariable String codigo, @PathVariable(required = false) Integer width) throws IOException {
        SimpleFeature predioFeature = prediosService.predioFeature(codigo);
        BBOX bbx = prediosService.getBBoxFeature(predioFeature);
        if (bbx == null) {
            System.out.println("Bounding Box null for  " + codigo);
            return null;
        }
        if (width == null) {
            width = 768;
        }
        if (width == 0) {
            width = 768;
        }
        StringBuilder imageUrl = new StringBuilder(properties.getWmsUrl());
        imageUrl.append("?service=WMS")
                .append("&version=").append(properties.getWmsVersion())
                .append("&&request=GetMap")
                .append("&layers=").append(properties.getPredioLayerColindantes())
                .append("&bbox=").append(bbx.getMinX()).append(",").append(bbx.getMinY()).append(",").append(bbx.getMaxX()).append(",").append(bbx.getMaxY())
                .append("&width=").append(width)
                .append("&height=").append(WmsImageUtils.heightCalculate(bbx, width).toString())
                .append("&srs=").append(properties.getSrid())
                .append("&format=image/png")
                .append("&env=").append(properties.getEnvFilter()).append(":").append(codigo);

        SimpleFeatureIterator predioColl = prediosService.predioFeatureColindantes(codigo, predioFeature);
        if (predioColl != null) {
            int colNum = 1;
            while (predioColl.hasNext()) {
                SimpleFeature next = predioColl.next();
                String cod = (String) next.getAttribute("codigo");
                if (!cod.equals(codigo)) {
                    imageUrl.append(";col").append(colNum).append(":").append(cod);
                    colNum++;
                }
            }
        }
        try {
            System.out.println("Execute url get image " + imageUrl.toString());
            URL url = new URL(imageUrl.toString());
            ResponseEntity<byte[]> responseEntity;
            InputStream is = url.openStream();
            HttpHeaders headers = new HttpHeaders();
            byte[] media = IOUtils.toByteArray(is);
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            responseEntity = new ResponseEntity<>(media, headers, org.springframework.http.HttpStatus.OK);
            return responseEntity;
        } catch (IOException iOException) {
            LOG.log(Level.SEVERE, codigo, iOException);
            return null;
        }
    }

    @RequestMapping(value = "/codigo/colindantes/{codigo}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getCodigoColindantes(@PathVariable String codigo, @PathVariable(required = false) Integer width) throws IOException {
        SimpleFeature predioFeature = prediosService.predioFeature(codigo);
        SimpleFeatureIterator predioColl = prediosService.predioFeatureColindantes(codigo, predioFeature);
        List<String> codigosColindantes = null;
        if (predioColl != null) {
            codigosColindantes = new ArrayList<>();
            while (predioColl.hasNext()) {
                SimpleFeature next = predioColl.next();
                String cod = (String) next.getAttribute("codigo");
                if (!cod.equals(codigo)) {
                    codigosColindantes.add(cod);
                }
            }
        }
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<List<String>> responseEntity = new ResponseEntity<>(codigosColindantes, headers, org.springframework.http.HttpStatus.OK);
        return responseEntity;
    }

}
