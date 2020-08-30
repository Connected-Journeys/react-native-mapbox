package biz.aliustaoglu.mapbox.MapBoxModule;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.collection.LongSparseArray;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import biz.aliustaoglu.mapbox.Utility.AssetsUtility;
import biz.aliustaoglu.mapbox.Utility.BitmapDownloader;
import biz.aliustaoglu.mapbox.Utility.OnAsyncTaskListener;

public class RNMBMarkers {
    final ReadableArray markers;
    MapboxMap mapboxMap;
    SymbolManager symbolManager;
    CircleManager pulsatorManager;
    CircleManager circleManager;

    private static final String COLOR_BLACK = "#140e26";
    private static final float NORMAL_TEXT_SIZE = 14f;

    Context context;
    Style style;

    final JsonParser parser = new JsonParser();

    public RNMBMarkers(ReadableArray markers) {
        this.markers = markers;
    }

    public void update(MapBoxMapView mapBoxMapView) {
        this.mapboxMap = mapBoxMapView.mapboxMap;
        this.symbolManager = mapBoxMapView.symbolManager;
        this.pulsatorManager = mapBoxMapView.pulsatorManager;
        this.circleManager = mapBoxMapView.circleManager;
        this.context = mapBoxMapView.getContext();
        this.style = mapBoxMapView.style;

        List<String> markerIDs = getMarkerIDs();

        for (int i = 0; i < markers.size(); i++) {
            ReadableMap marker = markers.getMap(i);
            String markerID = Objects.requireNonNull(marker).getString("id");
            markerIDs.remove(markerID);
            setMarker(marker);
            setPulsator(marker);
        }

        removeMarkers(markerIDs);
        removePulsators(markerIDs);
    }

    private List<String> getMarkerIDs() {
        LongSparseArray<Symbol> symbolArray = this.symbolManager.getAnnotations();
        LongSparseArray<Circle> circleArray = this.circleManager.getAnnotations();

        List<String> symbolIDs = new ArrayList<>();
        for (int i = 0; i < symbolArray.size(); i++) {
            String s = Objects.requireNonNull(symbolArray.valueAt(i).getData()).getAsJsonObject().get("id").getAsString();
            symbolIDs.add(s);
        }
        for (int i = 0; i < circleArray.size(); i++) {
            String s = Objects.requireNonNull(circleArray.valueAt(i).getData()).getAsJsonObject().get("id").getAsString();
            symbolIDs.add(s);
        }
        return symbolIDs;
    }

    private void setPulsator(ReadableMap marker) {
        final String id = marker.getString("id");
        final double lat = marker.getDouble("lat");
        final double lng = marker.getDouble("lng");
        final boolean canPulse = marker.hasKey("pulsator");
        final ReadableMap pulsator = canPulse ? marker.getMap("pulsator") : null;

        if (!canPulse) return;

        Circle currentPulsator = null;
        LongSparseArray<Circle> pulsators = this.pulsatorManager.getAnnotations();
        for (int i = 0; i < pulsators.size(); i++) {
            String pulsatorId = Objects.requireNonNull(pulsators.valueAt(i).getData()).getAsJsonObject().get("id").getAsString();
            if (pulsatorId.equals(id)) {
                currentPulsator = pulsators.valueAt(i);
                break;
            }
        }
        if (currentPulsator == null) {
            JsonElement pulsatorData = parser.parse("{\"id\":\"" + id + "\"}");
            CircleOptions pulsatorOptions = new CircleOptions()
                .withGeometry(Point.fromLngLat(lng, lat))
                .withData(pulsatorData)
                .withCircleOpacity(0.3f)
                .withCircleBlur(0.1f);

            if (Objects.requireNonNull(pulsator).hasKey("color"))
                pulsatorOptions = pulsatorOptions.withCircleColor(pulsator.getString("color"));


            final Circle pulsatorCircle = this.pulsatorManager.create(pulsatorOptions);

            ValueAnimator pulseAnimator = ValueAnimator.ofFloat(0f, 20f);
            pulseAnimator.setDuration(1500);
            pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pulseAnimator.setRepeatMode(ValueAnimator.RESTART);
            pulseAnimator.start();

            pulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float val = (float) animation.getAnimatedValue();
                    pulsatorCircle.setCircleRadius(val);
                    pulsatorManager.update(pulsatorCircle);
                }
            });

        } else {
            currentPulsator.setGeometry(Point.fromLngLat(lng, lat));
            this.pulsatorManager.update(currentPulsator);
        }


    }

    // If removed from props, remove from map as well
    private void removeMarkers(List<String> markerIDs) {
        LongSparseArray<Symbol> symbols = this.symbolManager.getAnnotations();
        LongSparseArray<Circle> circles = this.circleManager.getAnnotations();

        for (int i = 0; i < markerIDs.size(); i++) {
            for (int j = 0; j < symbols.size(); j++) {
                Symbol symbol = symbols.get(symbols.keyAt(0));
                assert symbol != null;
                String id = Objects.requireNonNull(symbol.getData()).getAsJsonObject().get("id").getAsString();
                if (id.equals(markerIDs.get(i))) {
                    this.symbolManager.delete(symbol);
                }
            }
            for (int j = 0; j < circles.size(); j++) {
                Circle circle = circles.get(circles.keyAt(0));
                assert circle != null;
                String id = Objects.requireNonNull(circle.getData()).getAsJsonObject().get("id").getAsString();
                if (id.equals(markerIDs.get(i))) {
                    this.circleManager.delete(circle);
                }
            }
        }

    }

    private void removePulsators(List<String> symbolIDs) {
        LongSparseArray<Circle> pulsators = this.pulsatorManager.getAnnotations();
        for (int i = 0; i < symbolIDs.size(); i++) {
            for (int j = 0; j < pulsators.size(); j++) {
                Circle circle = pulsators.get(pulsators.keyAt(j));
                assert circle != null;
                String id = Objects.requireNonNull(circle.getData()).getAsJsonObject().get("id").getAsString();
                if (id.equals(symbolIDs.get(i))) {
                    this.pulsatorManager.delete(circle);
                }
            }
        }
    }

    /**
     * Set markers
     *
     * @param marker - Readable props
     */
    private void setMarker(ReadableMap marker) {
        final String id = marker.getString("id");
        final double lat = marker.getDouble("lat");
        final double lng = marker.getDouble("lng");
        final String strIcon = marker.hasKey("icon") ? Objects.requireNonNull(marker.getMap("icon")).getString("uri") : null;
        final float scale = marker.hasKey("icon") ? (float) Objects.requireNonNull(marker.getMap("icon")).getDouble("scale") : 1f;

        final Map labelProperties = marker.hasKey("label") ? Objects.requireNonNull(marker.getMap("label")).toHashMap() : null;

        final LatLng latLng = new LatLng(lat, lng);

        final Float[] iconOffset = marker.hasKey("centerOffset") ?
            new Float[]{(float) Objects.requireNonNull(marker.getArray("centerOffset")).getDouble(0), (float) Objects.requireNonNull(marker.getArray("centerOffset")).getDouble(1)}
            : new Float[]{0f, 0f};

        // Default icons
        if (strIcon == null) {
            if (marker.hasKey("circle")) setCircleIcon(marker.getMap("circle"), id, latLng);
        }
        // Debug
        else if (strIcon.startsWith("http")) {

            BitmapDownloader bd = new BitmapDownloader(new OnAsyncTaskListener<Bitmap>() {
                @Override
                public void onAsyncTaskSuccess(Bitmap bm) {
                    setSymbolIcon(id, strIcon, bm, latLng, 2f/scale, iconOffset, labelProperties);
                }

                @Override
                public void onAsyncTaskFailure(Exception e) {

                }
            });
            bd.execute(strIcon);
        } else {
            // Prod
            AssetsUtility assetsUtility = new AssetsUtility(this.context);
            int resourceId = assetsUtility.getAssetFromResource(strIcon);
            Bitmap bm = BitmapFactory.decodeResource(this.context.getResources(), resourceId);
            setSymbolIcon(id, strIcon, bm, latLng, 1f/scale, iconOffset, labelProperties);
        }
    }


    /**
     * Set symbol icon for annotation. Before creating a new one first check if symbol exits. If so update the existing one
     *  @param id         Marker id - comes from props
     * @param iconName   Icon name
     * @param bm         Bitmap object
     * @param latLng     Latitude and longitude - comes from props
     * @param iconSize   - icon size
     * @param iconOffset - icon offset
     * @param labelProperties - label properties
     */
    private void setSymbolIcon(String id, String iconName, Bitmap bm, LatLng latLng, Float iconSize, Float[] iconOffset, Map labelProperties) {
        Objects.requireNonNull(this.mapboxMap.getStyle()).addImage(iconName, bm);
        LongSparseArray<Symbol> annotations = this.symbolManager.getAnnotations();
        boolean hasSymbol = false;

        Symbol currentAnnotation = null;

        for (int i = 0; i < annotations.size(); i++) {
            JsonObject data = (JsonObject) annotations.valueAt(i).getData();
            currentAnnotation = annotations.valueAt(i);
            if (Objects.requireNonNull(data).get("id").getAsString().equals(id)) {
                hasSymbol = true;
                break;
            }
        }

        if (!hasSymbol) {
            JsonElement symbolData = parser.parse("{\"id\":\"" + id + "\"}");

            SymbolOptions options = new SymbolOptions().withLatLng(latLng)
                .withIconImage(iconName)
                .withIconSize(iconSize)
                .withData(symbolData)
                .withIconOffset(iconOffset);

            if (labelProperties != null) {
                options.withTextField(labelProperties.containsKey("labelText") ? labelProperties.get("labelText").toString() : "");
                options.withTextColor(labelProperties.containsKey("color") ? labelProperties.get("color").toString() : COLOR_BLACK);
                options.withTextSize(labelProperties.containsKey("textSize") ? new Float(labelProperties.get("textSize").toString()) : NORMAL_TEXT_SIZE);
                options.withTextOffset(new Float[]{0f, 1.15f});
                options.withTextFont(new String[]{"Open Sans Bold", "Arial Unicode MS Bold"});
            }
            symbolManager.create(options);

        } else {
            currentAnnotation.setLatLng(latLng);
            symbolManager.update(currentAnnotation);
        }
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setIconIgnorePlacement(true);
    }

    private void setCircleIcon(ReadableMap circle, String id, LatLng latLng) {
        LongSparseArray<Circle> circleAnnotations = this.circleManager.getAnnotations();
        boolean hasSymbol = false;
        Circle currentCircle = null;

        for (int i = 0; i < circleAnnotations.size(); i++) {
            JsonObject data = (JsonObject) circleAnnotations.valueAt(i).getData();
            currentCircle = circleAnnotations.valueAt(i);
            if (Objects.requireNonNull(data).get("id").getAsString().equals(id)) {
                hasSymbol = true;
                break;
            }
        }

        if (!hasSymbol) {
            JsonElement circleData = parser.parse("{\"id\":\"" + id + "\"}");
            CircleOptions circleOptions = new CircleOptions()
                .withLatLng(latLng)
                .withData(circleData);
            if (circle.hasKey("color"))
                circleOptions = circleOptions.withCircleColor(circle.getString("color"));
            if (circle.hasKey("radius"))
                circleOptions = circleOptions.withCircleRadius((float) circle.getDouble("radius"));
            this.circleManager.create(circleOptions);
        } else {
            currentCircle.setLatLng(latLng);
        }
    }
}
