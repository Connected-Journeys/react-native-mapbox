package biz.aliustaoglu.mapbox.MapBoxModule;

import android.graphics.Color;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import java.util.ArrayList;
import java.util.List;

public class RNMBPolylines {

    ReadableArray polylines;
    String prefix = "RNMB";
    private static final String ROAD_LABEL_LAYER_ID = "road-label";

    public RNMBPolylines(ReadableArray polylines) {
        this.polylines = polylines;
    }

    public void update(MapBoxMapView mapBoxMapView) {
        Style style = mapBoxMapView.mapboxMap.getStyle();
        if (style != null) {
            while(!style.isFullyLoaded()) {}
            updatePolylines(style);
        }
    }

    private void updatePolylines(Style style) {
        List<String> sourcesToRemove = getSourceIDs(style);

        for (int i = 0; i < this.polylines.size(); i++) {

            ReadableMap polyline = polylines.getMap(i);
            ReadableMap properties = polyline.getMap("properties");
            String id = properties.getString("id");
            String sourceID = prefix + "-source-" + id;

            GeoJsonSource source = (GeoJsonSource) style.getSource(sourceID);

            if (source != null) {
                updateSource(source, polyline);
                sourcesToRemove.remove(sourceID);
            } else {
                addSourceAndLayer(id, polyline, properties, style);
            }
        }
        // If a polyline is removed from the props, remove it from the map as well
        if (sourcesToRemove.size()>0) removeSources(sourcesToRemove, style);

    }

    // Replace source with an empty feature
    private void removeSources(List<String> sourceIDs, Style style){
        for (int i=0;i<sourceIDs.size();i++){
            GeoJsonSource source = (GeoJsonSource) style.getSource(sourceIDs.get(i));
            // Empty feature
            source.setGeoJson("{\n" +
                "  \"type\": \"Feature\",\n" +
                "  \"geometry\": {\n" +
                "    \"type\": \"LineString\",\n" +
                "    \"coordinates\": []\n" +
                "  }\n" +
                "}");
        }
    }

    private List<String> getSourceIDs(Style style){
        List<Source> sources = style.getSources();
        List<String> sourceIDs = new ArrayList<>();
        for(int i=0;i<sources.size();i++){
            String id = sources.get(i).getId();
            if (id.startsWith(prefix)) {
                sourceIDs.add(id);
            }
        }
        return sourceIDs;
    }

    private void updateSource(GeoJsonSource source, ReadableMap polyline){
        source.setGeoJson(getJSON(polyline));
    }

    private void addSourceAndLayer(String id, ReadableMap polyline, ReadableMap properties, Style style){
        String sourceId = prefix + "-source-" + id;
        String lineId = prefix + "-line-" + id;
        getJSON(polyline);

        Feature feature = Feature.fromJson(getJSON(polyline));
        style.addSource(new GeoJsonSource(sourceId, feature));

        PropertyValue<?>[] propertyValues = getPropertyValues(properties);

        LineLayer lineLayer = new LineLayer(lineId, sourceId).withProperties(propertyValues);
        style.addLayerBelow(lineLayer, ROAD_LABEL_LAYER_ID);
    }

    /**
     * Construct optional varargs values for LineLayer.withProperties(...)
     * @param properties Readable map of properties
     * @return Property values
     */
    private PropertyValue<?>[] getPropertyValues(ReadableMap properties) {
        List<PropertyValue<?>> propertyValues = new ArrayList<>();

        propertyValues.add(PropertyFactory.lineJoin(Property.LINE_JOIN_MITER));
        if (properties.hasKey("lineWidth"))
            propertyValues.add(PropertyFactory.lineWidth((float) properties.getDouble("lineWidth")));
        if (properties.hasKey("lineColor"))
            propertyValues.add(PropertyFactory.lineColor(Color.parseColor(properties.getString("lineColor"))));
        if (properties.hasKey("lineType")) {
            if (properties.getString("lineType").equals("dash")) {
                propertyValues.add(PropertyFactory.lineDasharray(new Float[]{1f, 1f}));
            }
        }

        return propertyValues.toArray(new PropertyValue<?>[propertyValues.size()]);
    }

    // ReadableMap.toString() adds an extra string. Remove it and get bare json.
    private String getJSON(ReadableMap readableMap) {
        String json = readableMap.toString();
        json = json.replaceAll("\\{ NativeMap: ", "");
        json = json.substring(0, json.lastIndexOf('}'));
        return json;
    }

}
