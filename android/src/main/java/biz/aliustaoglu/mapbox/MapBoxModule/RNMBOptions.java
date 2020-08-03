package biz.aliustaoglu.mapbox.MapBoxModule;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.PermissionListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.pluginscalebar.ScaleBarOptions;
import com.mapbox.pluginscalebar.ScaleBarPlugin;

import java.util.List;

public class RNMBOptions {
    ReadableMap options;

    public RNMBOptions(ReadableMap options) {
        this.options = options;
    }

    public void update(MapboxMap mapboxMap, MapView mapView, final Context context) {
        Boolean showsUserHeadingIndicator = false;
        Boolean showsScale = false;
        Boolean showsHeading = false;
        Boolean showsUserLocation = false;

        if (options.hasKey("showsUserHeadingIndicator"))
            showsUserHeadingIndicator = options.getBoolean("showsUserHeadingIndicator");
        if (options.hasKey("showsScale")) showsScale = options.getBoolean("showsScale");
        if (options.hasKey("showsHeading")) showsHeading = options.getBoolean("showsHeading");
        if (options.hasKey("showsUserLocation"))
            showsUserLocation = options.getBoolean("showsUserLocation");

        if (showsScale) {
            ScaleBarPlugin scaleBarPlugin = new ScaleBarPlugin(mapView, mapboxMap);
            scaleBarPlugin.create(new ScaleBarOptions(context));
        }

        if (!showsUserLocation) return;


        final LocationComponent locationComponent = mapboxMap.getLocationComponent();
        final Boolean headingIndicator = showsUserHeadingIndicator;


        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if (headingIndicator == true) {
                    locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(context, style).build());
                    locationComponent.setLocationComponentEnabled(true);
                    locationComponent.setCameraMode(CameraMode.NONE);
                    locationComponent.setRenderMode(RenderMode.COMPASS);
                }
            }
        });


    }

}
