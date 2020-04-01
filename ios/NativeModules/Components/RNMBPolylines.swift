//
//  RNMBPolylines.swift
//  RNMBReactNativeMapbox
//
//  Created by Cuneyt Aliustaoglu on 16/01/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import Mapbox

class RNMBPolylines {
    let polylines: NSArray!
    let oldPolylines: NSArray!
    var mapView: MGLMapView!
    var removedIDs: [String] = []
    
    
    init(_ polylines: NSArray, _ oldValue: NSArray) {
        self.polylines = polylines
        self.oldPolylines = oldValue
        
        // Detect removed polylines(from props) and clear them off the map too
        oldPolylines.forEach { oldPoly in
            let oldDic = oldPoly as! NSDictionary
            let id = (oldDic.object(forKey: "properties") as! NSDictionary).object(forKey: "id") as! String
            let con = polylines.contains{ p in
                let poly = p as! NSDictionary
                let containsID = id == (poly.object(forKey: "properties") as! NSDictionary).object(forKey: "id") as! String
                return containsID
            }
            if !con {
                removedIDs.insert(id, at: 0)
            }
        }
    }
    
    public func update(_ mapView: MGLMapView){
        self.mapView = mapView
        guard let style = self.mapView.style else { return }
        
        
        self.polylines.forEach{ p in
            
            
            let polyline = p as! NSDictionary
            let properties = polyline.object(forKey: "properties") as! NSDictionary
            let id = properties.object(forKey: "id") as! String
            let jsonData = try? JSONSerialization.data(withJSONObject: polyline, options: [])
            
            let countCoords = ((polyline.object(forKey: "geometry") as? NSDictionary)?.object(forKey: "coordinates") as? NSArray)?.count ?? 0
            print(countCoords)
            
            if (countCoords>0) {
                let shapeFromGeoJSON = try? MGLShape(data: jsonData!, encoding: String.Encoding.utf8.rawValue)
                
                if (shapeFromGeoJSON != nil) {
                    if let existingSource = style.source(withIdentifier: "RNMB-shape-\(id)") as? MGLShapeSource {
                        existingSource.shape = shapeFromGeoJSON
                    } else {
                        let source = MGLShapeSource(identifier: "RNMB-shape-\(id)", shape: shapeFromGeoJSON, options: nil)
                        style.addSource(source)
                        addLayer(id, polyline, properties, source)
                    }
                }
            }
            
            
        }
        
        // Remove layers (if removed from props)
        removeLayers(style)
    }
    
    private func removeLayers(_ style: MGLStyle){
        self.removedIDs.forEach{ id in
            if let existingSource = style.source(withIdentifier: "RNMB-shape-\(id)") as? MGLShapeSource {
                existingSource.shape = nil
            }
        }
        self.removedIDs = []
    }
    
    private func addLayer(_ id: String, _ dict: NSDictionary, _ properties: NSDictionary, _ source: MGLShapeSource){
        let style = self.mapView.style
        let layer = MGLLineStyleLayer(identifier: "polyline\(id)", source: source)
        let color = properties.object(forKey: "lineColor") as? String ?? "#336cca"
        let width = properties.object(forKey: "lineWidth") as? Float ?? 1.0
        if let pattern = properties.object(forKey: "lineType") {
            if (pattern as! String) == "dash" {
                layer.lineDashPattern = NSExpression(forConstantValue: [0.75, 0.75])
            }
        }
        
        layer.lineWidth = NSExpression(forConstantValue: width)
        layer.lineColor = NSExpression(forConstantValue: hexStringToUIColor(hex: color) )
        
        style?.addLayer(layer)
        
    }
    
    
}
