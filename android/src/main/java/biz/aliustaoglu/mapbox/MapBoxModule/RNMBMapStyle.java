package biz.aliustaoglu.mapbox.MapBoxModule;

import com.facebook.react.bridge.ReadableMap;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.building.BuildingPlugin;


public class RNMBMapStyle {
    String mapStyle;

    public RNMBMapStyle() {
        this.mapStyle = "DEFAULT";
    }

    public RNMBMapStyle(ReadableMap mapStyle){
        this.mapStyle = mapStyle.hasKey("styleName") ? mapStyle.getString("styleName") : "DEFAULT";
    }

    public void update(MapboxMap mapboxMap){
        switch (mapStyle.toUpperCase()){
            case "OUTDOORS": mapboxMap.setStyle(Style.OUTDOORS);break;
            case "LIGHT": mapboxMap.setStyle(Style.LIGHT);break;
            case "DARK": mapboxMap.setStyle(Style.DARK);break;
            case "SATELLITE": mapboxMap.setStyle(Style.SATELLITE);break;
            case "SATELLITE_STREETS": mapboxMap.setStyle(Style.SATELLITE_STREETS);break;
            case "TRAFFIC_DAY": mapboxMap.setStyle(Style.TRAFFIC_DAY);break;
            default: mapboxMap.setStyle(Style.MAPBOX_STREETS);
        }


    }

    public void update(MapboxMap mapboxMap, MapView mapView, Style style){
        BuildingPlugin buildingPlugin = new BuildingPlugin(mapView, mapboxMap, style);
        buildingPlugin.setOpacity(0.05f);
        buildingPlugin.setVisibility(true);
    }
}
