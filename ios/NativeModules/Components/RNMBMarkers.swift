//
//  RNMBMarkers.swift
//  RNMBReactNativeMapbox
//
//  Created by Cüneyt Aliustaoğlu on 12/01/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import Mapbox
import Pulsator



class RNMBPointAnnotation: MGLPointAnnotation {
    var lat: CLLocationDegrees
    var lng: CLLocationDegrees
    var icon: NSDictionary?
    //var annotationImage: MGLAnnotationImage?
    var annotationView: MGLAnnotationView?
    var id: String
    var puls: NSDictionary?
    var label: NSDictionary?
    init(_ marker: NSDictionary){
        self.lat = marker.object(forKey: "lat") as! CLLocationDegrees
        self.lng = marker.object(forKey: "lng") as! CLLocationDegrees
        self.id = marker.object(forKey: "id") as! String
        self.puls = marker.object(forKey: "pulsator") as? NSDictionary
        self.label = marker.object(forKey: "label") as? NSDictionary
        super.init()

        if let ico = marker.object(forKey: "icon") {
            let icon = ico as? NSDictionary
            self.icon = icon
            let uri = icon?.object(forKey: "uri") as! String
            let scale = icon?.value(forKey: "scale") as! CGFloat

            let url = URL(string: uri)
            let data = try? Data(contentsOf: url!)
            let img = UIImage(data: data!, scale: scale)!
            let imgView = UIImageView(image: img)

            self.annotationView = MGLAnnotationView(reuseIdentifier: self.id)
            imgView.center = self.annotationView!.center

            if let centerOffset = marker.object(forKey: "centerOffset") as? NSArray {
                self.annotationView?.centerOffset = CGVector(dx: centerOffset[0] as! Double, dy: centerOffset[1] as! Double)
            }

            if(self.label != nil ) {
                let detailsLabel:UILabel = UILabel()
                let title = self.label!.object(forKey: "labelText") as? String
                detailsLabel.text = title
                detailsLabel.textAlignment = .center
                if let color = self.label!.object(forKey: "color") {
                    detailsLabel.textColor = hexStringToUIColor(hex: color as! String)
                } else {
                    detailsLabel.textColor = UIColor(red: 0.1, green: 0.24, blue: 1, alpha: 1)
                }
                if let textSize = self.label!.object(forKey: "textSize") {
                    detailsLabel.font = UIFont(name: "HelveticaNeue-CondensedBlack", size: textSize as! CGFloat)
                } else {
                    detailsLabel.font = UIFont(name: "HelveticaNeue-CondensedBlack", size: 10)
                }
                detailsLabel.clipsToBounds = true
                if let frame = self.label!.object(forKey: "frame") {
                    let labelFrame = frame as! NSDictionary
                    let x = labelFrame.object(forKey: "x") as? CGFloat ?? 0
                    let y = labelFrame.object(forKey: "y") as? CGFloat ?? 0
                    let width = labelFrame.object(forKey: "width") as? CGFloat ?? 40
                    let height = labelFrame.object(forKey: "height") as? CGFloat ?? 20
                    detailsLabel.frame = CGRect(x: x, y: y, width: width, height: height)

                } else {
                    detailsLabel.frame = CGRect(x: 10, y: 27, width: 25, height: 14)
                }
                if let backgroundColor = self.label!.object(forKey: "backgroundColor") {
                    detailsLabel.backgroundColor =  hexStringToUIColor(hex: backgroundColor as! String)
                } else {
                    detailsLabel.backgroundColor =  UIColor.white.withAlphaComponent(1.0)
                }
                if let border = self.label!.object(forKey: "border") {
                    let borderOb = border as! NSDictionary
                    let borderColor = borderOb.object(forKey: "color") as? String ?? "#140e26"
                    let borderWidth = borderOb.object(forKey: "width") as? CGFloat ?? 0.05
                    let borderCornerRadius = borderOb.object(forKey: "cornerRadius") as? CGFloat ?? 5.0
                    detailsLabel.layer.borderColor = hexStringToCGColor(hex:  borderColor )
                    detailsLabel.layer.borderWidth = borderWidth
                    detailsLabel.layer.cornerRadius = borderCornerRadius
                } else {
                    detailsLabel.layer.cornerRadius = 5.0
                    detailsLabel.layer.borderWidth = 0.05
                    detailsLabel.layer.borderColor = UIColor.white.cgColor
                }
                imgView.addSubview(detailsLabel)
            }
            self.annotationView?.addSubview(imgView)
        } else if let crc = marker.object(forKey: "circle") {
            let circle = crc as! NSDictionary
            let radius = circle.object(forKey: "radius") as? Int ?? 9
            self.annotationView = MGLAnnotationView(reuseIdentifier: self.id)
            let circleView = UIView(frame: CGRect(x: 0, y: 0, width: radius*2, height: radius*2))
            circleView.center = self.annotationView!.center
            circleView.layer.cornerRadius = CGFloat(radius)
            if let circleColor = circle.object(forKey: "color") {
                circleView.backgroundColor = hexStringToUIColor(hex: circleColor as! String)
            }
            self.annotationView?.addSubview(circleView)

        }

        if (self.puls != nil && self.annotationView?.subviews.count ?? 0>0){
            let pulsator = Pulsator()
            let pulsatorParent = self.annotationView!.subviews[0]
            let posX = pulsatorParent.frame.size.width/2
            let posY = pulsatorParent.frame.size.height/2
            pulsator.position = CGPoint(x: posX, y: posY)

            if let pulsColor = puls!.object(forKey: "color") {
                pulsator.backgroundColor = hexStringToCGColor(hex: pulsColor as! String)
            } else {
                pulsator.backgroundColor = UIColor(red: 0.1, green: 0.24, blue: 1, alpha: 1).cgColor
            }

            if let pulsRadius = puls!.object(forKey: "radius") {
                pulsator.radius = CGFloat(pulsRadius as! Float)
            }

            if let pulsDuration = puls!.object(forKey: "duration") {
                pulsator.duration = CFTimeInterval(pulsDuration as! Float)
            }

            pulsatorParent.layer.addSublayer(pulsator)

            pulsator.start()
        }

        self.title = marker.object(forKey: "title") as? String
        self.subtitle = marker.object(forKey: "subtitle") as? String
        self.coordinate = CLLocationCoordinate2D(latitude: self.lat, longitude: self.lng)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class RNMBMarkers{
    var markers: NSArray = []
    var oldMarkers: NSArray = []
    var imgAnnotation: MGLAnnotationImage!
    var removedIDs: [String] = []

    init(_ markers: NSArray, _ oldValue: NSArray) {
        self.markers = markers
        self.oldMarkers = oldValue

        self.oldMarkers.forEach{ oldMarker in
            let oldDic = oldMarker as! NSDictionary
            let id = oldDic.object(forKey: "id") as! String
            let containsId = markers.contains { m in
                let marker = m as! NSDictionary
                let containsId = id == (marker.object(forKey: "id") as! String)
                return containsId
            }
            if !containsId {
                self.removedIDs.append(id)
            }
        }


    }

    public func update(_ mapView: MGLMapView){
        for m in self.markers {
            let marker = m as! NSDictionary
            let id = marker.object(forKey: "id") as! String


            let existingAnnotation = mapView.annotations?.first{ ann in
                let annotation = ann as! RNMBPointAnnotation
                return annotation.id == id
                } as? RNMBPointAnnotation

            if (existingAnnotation == nil) {
                mapView.addAnnotation(RNMBPointAnnotation(marker))
            } else {
                let pin = RNMBPointAnnotation(marker)
                existingAnnotation!.coordinate = pin.coordinate
            }
        }

        // If remcd ..mapViewoved from props, remove from map as well
        removeMarkers(mapView)
    }

    private func removeMarkers(_ mapView: MGLMapView){
        self.removedIDs.forEach{ id in
            let annotationToDelete = mapView.annotations?.first{ann in
                let annotation = ann as! RNMBPointAnnotation
                return annotation.id == id
            }
            if (annotationToDelete != nil) {
                mapView.removeAnnotation(annotationToDelete!)
            }
        }
        self.removedIDs = []
    }
}

func hexStringToCGColor (hex:String) -> CGColor {
    return hexStringToUIColor(hex: hex).cgColor
}

func hexStringToUIColor (hex:String) -> UIColor {
    var cString:String = hex.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()

    if (cString.hasPrefix("#")) {
        cString.remove(at: cString.startIndex)
    }

    if ((cString.count) != 6) {
        return UIColor.gray
    }

    var rgbValue:UInt64 = 0
    Scanner(string: cString).scanHexInt64(&rgbValue)

    return UIColor(
        red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
        green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
        blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
        alpha: CGFloat(1.0)
    )
}

