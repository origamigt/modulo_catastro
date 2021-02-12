package org.geoapi.utils;

import org.opengis.filter.spatial.BBOX;

public class WmsImageUtils {

    public static Integer heightCalculate(BBOX bbox, Integer width){
        /**
         *  w     xdim
         *  ?     ydim
         */
        Double xdim = bbox.getMaxX() - bbox.getMinX();
        Double ydim = bbox.getMaxY() - bbox.getMinY();
        Double w = width.doubleValue();

        Double h = ((w * ydim)/xdim);

        return h.intValue();
    }

}
