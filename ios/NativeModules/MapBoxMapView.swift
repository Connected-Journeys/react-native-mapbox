import Foundation
import UIKit
import Mapbox
import CoreLocation
import UserNotifications

class MapBoxMapView: GenericMap, MGLMapViewDelegate {
    private var mapView: MGLMapView?
    
    override init(frame:CGRect) {
        super.init(frame: UIScreen.main.bounds)
        util = Utils()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func initMap() {
        self.isMapReady = true
        let url = URL(string: "mapbox://styles/mapbox/streets-v11")
        mapView = MGLMapView(frame: self.bounds, styleURL: url)
        mapView!.delegate = self
        
        // JS props
        updateRegion()
        updateMarkers()
        updateOptions()
        
        // JS events
        if self.onMapReady != nil {
            self.onMapReady!([:])
        }
        
        self.addSubview(mapView!)
    }
    
    override func updateZoom() {
        mapView?.setZoomLevel(self.zoom, animated: true)
    }
    
    override func updateRegion() {
        mapView!.setCenter(CLLocationCoordinate2D(latitude: self.lat, longitude: self.lng), zoomLevel: self.zoom, animated: true)
    }
    
    override func updateMarkers() {
        for m in self.markers{
            let marker = m as! NSDictionary
            let lat = marker.value(forKey: "lat") as! Double
            let lng = marker.value(forKey: "lng") as! Double
            let title = marker.value(forKey: "title") as! String
            let subTitle = marker.value(forKey: "subTitle") as! String
            
            let pin = MGLPointAnnotation()
            pin.coordinate = CLLocationCoordinate2D(latitude: lat, longitude: lng)
            pin.title = title
            pin.subtitle = subTitle
            mapView!.addAnnotation(pin)
            
        }
        
    }

    override func updateOptions() {
        mapView!.showsUserHeadingIndicator = true
        mapView!.showsScale = true
        mapView!.showsHeading = true
        mapView!.showsUserLocation = true
    }
    
    public func locateUser(location:NSArray, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock){
        let coords = location[0] as! NSDictionary
        
        mapView!.setCenter(CLLocationCoordinate2D(latitude: CLLocationDegrees(self.lat), longitude: CLLocationDegrees(self.lng)), zoomLevel: self.zoom, animated: true)
        util?.returnPromise(success: true, response: true, resolve: resolve, reject: reject)
    }
    
    func mapView(_ mapView: MGLMapView, annotationCanShowCallout annotation: MGLAnnotation) -> Bool {
    return true
    }
    
}
