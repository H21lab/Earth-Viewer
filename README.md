# Earth-Viewer
Android application.
Animated planet Earth with live weather and satellite data.

![alt tag](https://github.com/H21lab/Earth-Viewer/blob/master/earth_viewer.png)

Imagery included:
Climate Reanalyzer weather forecast
- World GFS Precipation and Clouds (+48h)
- World GFS Air Temperature (+48h)
- World GFS Air Temperature Anomaly (+48h)
- World GFS Precipitable Water (+48h)
- World GFS Surface Wind Speed (+48h)
- World GFS Jetstream Wind Speed (+48h)
- World GFS Snow Depth (+48h)
US Naval Research Laboratory, Marine Meteorology Division
- World Rainrate (-24h, generated every 3h)

METEOSAT 0 degree satellite
- Airmass realtime imagery (-24h, generated every 1h)
- Airmass realtime imagery full resolution (-6h, generated every 1h)
- Multi-Sensor Precipitation Estimate (rain rate) (-24h, generated every 15min)
- IR 10.8 (-24h, generated every 1h)

METEOSAT IODC satellite
- Multi-Sensor Precipitation Estimate (rain rate) (-24h, generated every 30min)

NASA GOES satellite
- Goes East Infrared (-24h, generated every 3h)
- Goes West Infrared (-24h, generated every 3h)

MTSAT satellite
- Infrared (-24h, generated every 1h)

SSEC
- Infrared low res global composite (-1w, generated every 3h)
- Water vapor low res global composite (-1w, generated every 3h)


Application features:
- Interpolation between images
- Imagery selection from menu
- Live Sun light
- Bump mapping
- Data cache for offline use
- Double tap will stop/play animation
- No Ads


Copyright and credit
CCI data has been obtained using Climate Reanalyzer (http://cci-reanalyzer.org), Climate Change Institute, University of Maine, USA.
NRL DATA has been obtained using United States Naval Research Laboratory, Marine Meteorology Division (http://www.nrlmry.navy.mil)
All METEOSAT images shown in the application are subject to EUMETSAT copyright.
For all NASA GOES images credit to NOAA-NASA GOES Project.
For all MTSAT images credit to Japan Meteorological Agency.
For all SSEC images provided courtesy of University of Wisconsin-Madison Space Science and Engineering Center.
For Xplanet cloudmap many thanks to Hari Nair author of Xplanet.


Limitations
On some devices application will not launch and crash report is seen. This is caused in most cases by low graphical card capabilities or low amount of memory of the target device. Application use OpenGL ES 2.0 and extensive pixel shader with multitexturing.

Time of CCI data is sometimes shifted back by one day. For exact time always the time in the texture is relevant.

Application is distributed as local image viewer which is accessing public available content from internet on behalf of user. Data are internally cached and only delta is downloaded. There is no guarantee for the availability of the downloaded data and the application also works without internet connectivity.

Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

## Build Instruction

Import the project into Android Studio, build and run the application.

## Attribution

This code was created by Martin Kacer, H21 lab, Copyright 2016.
https://sites.google.com/site/h21lab

