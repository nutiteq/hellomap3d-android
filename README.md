# Update - new major map SDK version 4.0 available!

Nutiteq joined CARTO in early 2016. We suggest to develop your app using newer **CARTO Mobile Map SDK** version 4.x. It has all same features, plus more. The new and much advanced sample code here: https://github.com/CartoDB/mobile-android-samples/ and developer Getting Started guidelines can be found from https://carto.com/docs/carto-engine/mobile-sdk/getting-started 

* **Android maven repository for SDK 3.x does not work anymore**, you can get older SDK version releases from https://developer.nutiteq.com/downloads 


# Android sample app with Nutiteq SDK 3.0 (old version)

This project shows API and features of Nutiteq Maps SDK 3.0.

[![Build Status](https://travis-ci.org/nutiteq/hellomap3d-android.svg?branch=master)](https://travis-ci.org/nutiteq/hellomap3d-android)

## Download and documentation
  * Download - get libs/ folder from any sample project
  * [Browse API JavaDoc](http://nutiteq.github.io/hellomap3d-android/)

## Samples
### com.nutiteq.hellomap3
Shows minimal map features: create a map (using online vector base) and add a pin

### com.nutiteq.advancedmap3
There is more configuration for map to control and maximize performance. Default background map is online vector map.

 1. Basic pin map, same as hellomap3d
 1. Vector overlay with lines, points, polygons, markers, texts, balloons
 1. Map event listener - show balloon for clicks on vector elements or map. Separate single, long, double and dual click events
 1. 3D overlays: NML model, 3D city and 3D polygon
 1. Show offline vector map, global general map is bundled with app
 1. MBTiles offline map (vector or raster)
 1. Animated raster overlay, looping sample with online weather
 1. Static raster overlay - hillshading
 2. Aerial map (from Bing satellite)
 3. Custom raster datasource
 4. Custom popup balloon
 1. Offline routing using Graphhopper library. First select a graphhopper .map file from SD card. It requires 0.3 version graphhopper data, get sample package for [Ontario, Canada](https://dl.dropboxusercontent.com/u/3573333/mapdata/graphhopper-0.3/canada-ontario-gh3.zip). Click 2 points on map: route start and end, and it shows route and instructions.
 2. PackageManager for offline package downloads
 3. Basic WMS datasource


The project includes Nutiteq SDK 3.0 (beta), under commercial evaluation license. For commercial app licenses and customizaton requests please contact sales@nutiteq.com

![screenshot](http://share.gifyoutube.com/yan3Ll.gif)
