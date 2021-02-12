package org.geoapi.services;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoapi.config.ApplicationProperties;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.MultiValuedFilter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrediosService {

    private static final Logger LOG = Logger.getLogger(PrediosService.class.getName());
    @Autowired
    protected FeatureSourceFactory featureSf;
    @Autowired
    private ApplicationProperties properties;

    public SimpleFeature predioFeature(String claveCatastral) {
        try {
            SimpleFeatureSource source = featureSf.predioSource();
            if (source == null) {
                System.out.println("SimpleFeatureSource " + claveCatastral);
                return null;
            }
            Filter flt = CQL.toFilter(properties.getClaveAttrName() + " = '" + claveCatastral + "'");
            SimpleFeatureCollection fc = source.getFeatures(flt);
            if (!fc.isEmpty()) {
                SimpleFeature sf = fc.features().next();
                return sf;
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, claveCatastral, ex);
        } catch (CQLException ex) {
            Logger.getLogger(PrediosService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public SimpleFeatureIterator predioFeatureColindantes(String claveCatastral, SimpleFeature predioFeature) {
        try {
            SimpleFeatureSource source = featureSf.predioSource();
            if (source == null) {
                System.out.println("SimpleFeatureSource Colindantes " + claveCatastral);
                return null;
            }
            Query query = new Query("predios_tx");
            query.setFilter(CQL.toFilter("INTERSECTS(geom, " + predioFeature.getAttribute("geom") + ")"));
            SimpleFeatureCollection fc = source.getFeatures(query);
            if (!fc.isEmpty()) {
                return fc.features();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, claveCatastral, ex);
        } catch (CQLException ex) {
            Logger.getLogger(PrediosService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Ejemplo via:
     * https://gis.stackexchange.com/questions/77639/geotools-wfs-feature-retrieval
     *
     * @param claveCatastral
     * @return
     */
    public BBOX predioBbox(String claveCatastral) {
        SimpleFeature sf = predioFeature(claveCatastral);
        if (sf == null) {
            return null;
        }
        return getBBoxFeature(sf);
    }

    public BBOX predioBboxEnvelope(String claveCatastral) {
        SimpleFeature sf = predioFeature(claveCatastral);
        if (sf == null) {
            return null;
        }
        return getBBoxFeature(sf);
    }

    public BBOX getBBoxFeature(SimpleFeature sf) {
        if (sf == null) {
            return null;
        }
        BoundingBox boundResult = sf.getBounds();
        BBOX bbx = new BBOX() {
            @Override
            public String getPropertyName() {
                return null;
            }

            @Override
            public String getSRS() {
                return null;
            }

            @Override
            public double getMinX() {
                return boundResult.getMinX() - properties.getEnvelopeAdd();
            }

            @Override
            public double getMinY() {
                return boundResult.getMinY() - properties.getEnvelopeAdd();
            }

            @Override
            public double getMaxX() {
                return boundResult.getMaxX() + properties.getEnvelopeAdd();
            }

            @Override
            public double getMaxY() {
                return boundResult.getMaxY() + properties.getEnvelopeAdd();
            }

            @Override
            public BoundingBox getBounds() {
                return null;
            }

            @Override
            public Expression getExpression1() {
                return null;
            }

            @Override
            public Expression getExpression2() {
                return null;
            }

            @Override
            public MultiValuedFilter.MatchAction getMatchAction() {
                return null;
            }

            @Override
            public boolean evaluate(Object o) {
                return false;
            }

            @Override
            public Object accept(FilterVisitor filterVisitor, Object o) {
                return null;
            }
        };
        return bbx;
    }

}
