package org.geoapi.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoapi.config.ApplicationProperties;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeatureSourceFactory {

    private static final Logger LOG = Logger.getLogger(FeatureSourceFactory.class.getName());

    @Autowired
    private ApplicationProperties properties;

    public SimpleFeatureSource predioSource() throws IOException {
        SimpleFeatureSource source = null;
        try {
            String getCapabilities = properties.getWfsUrl() + "?service=wfs&version=" + properties.getWfsVersion() + "&request=GetCapabilities";
            Map connectionParameters = new HashMap();
            connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities);
            WFSDataStoreFactory dsf = new WFSDataStoreFactory();

            WFSDataStore dataStore = dsf.createDataStore(connectionParameters);
            source = dataStore.getFeatureSource(properties.getPredioLayer());
        } catch (IOException iOException) {
            LOG.log(Level.SEVERE, properties.getPredioLayer(), iOException);
        }
        return source;
    }

}
