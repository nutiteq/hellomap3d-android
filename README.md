# Android sample app with Nutiteq SDK 3.0


This project shows API and features of Nutiteq Maps SDK 3.0.

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
 1. Animated raster overlay, looping sample with online weather
 1. Static raster overlay - hillshading
 1. Offline routing using Graphhopper library. First select a graphhopper .map file from SD card. It requires 0.3 version graphhopper data, get sample package for [Ontario, Canada](https://dl.dropboxusercontent.com/u/3573333/mapdata/graphhopper-0.3/canada-ontario-gh3.zip). Click 2 points on map: route start and end, and it shows route and instructions.

The project includes Nutiteq SDK 3.0 (beta), under commercial evaluation license. For commercial app licenses and customizaton requests please contact sales@nutiteq.com

![screenshot](http://share.gifyoutube.com/yan3Ll.gif)
