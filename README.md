# Earth-Viewer
Android application.
Animated planet Earth with live weather, satellite data, global forecast and historical data. Application visualizes also data-sets useful for Global warming monitoring. 

![alt tag](https://github.com/H21lab/Earth-Viewer/blob/master/earth_viewer.png)

Imagery included:

Climate Reanalyzer weather forecast
- World GFS Precipitation and Clouds (+48h)
- World GFS Air Temperature (+48h)
- World GFS Air Temperature Anomaly (+48h)
- World GFS Precipitable Water (+48h)
- World GFS Surface Wind Speed (+48h)
- World GFS Jetstream Wind Speed (+48h)

Climate Reanalyzer historical data / Global warming monitoring
- CCI Temperature Anomaly CFSV2 2m (past 182 days)
- CCI Sea Surface Temperature Anomaly OISST v2 (past 182 days)
- Sea Surface Temperature Anomaly OISST v2 (past 35 years)
- Sea Surface Temperature Anomaly ERSST v5 (past 65 years)

METEOSAT 0 degree satellite
- Airmass realtime imagery (-24h, generated every 1h)
- Airmass realtime imagery full resolution (-6h, generated every 1h)
- Multi-Sensor Precipitation Estimate (rain rate) (-24h, generated every 15min)
- IR 10.8 (-24h, generated every 1h)

METEOSAT IODC satellite
- IR 10.8 (-24h, generated every 1h)

SSEC
- Infrared low res global composite (-1w, generated every 3h)
- Water vapor low res global composite (-1w, generated every 3h)

NOAA
- Aurora 30 Minute Forecast Northern Hemisphere (-24h)
- Aurora 30 Minute Forecast Southern Hemisphere (-24h)


Application features:
- Interpolation between images
- Imagery selection from menu
- Live Sun light
- Bump mapping
- Data cache for offline use
- Double tap will stop/play animation


Copyright and credit:
- CCI data has been obtained using Climate Reanalyzer (https://climatereanalyzer.org), Climate Change Institute, University of Maine, USA.
- NRL DATA has been obtained using United States Naval Research Laboratory, Marine Meteorology Division (http://www.nrlmry.navy.mil).
- All METEOSAT images shown in the application are subject to EUMETSAT copyright.
- For all MTSAT images credit to Japan Meteorological Agency.
- For all SSEC images provided courtesy of University of Wisconsin-Madison Space Science and Engineering Center.
- NOAA DATA are obtained from NOAA SPACE WEATHER PREDICTION CENTER (https://www.swpc.noaa.gov/).




Limitations
On some devices application will not launch and crash report is seen. This is caused in most cases by low graphical card capabilities or low amount of memory of the target device. Application use OpenGL ES 2.0 and extensive pixel shader with multitexturing.

Application is distributed as local image viewer which is accessing public available content from internet on behalf of user. Data are internally cached and only delta is downloaded. There is no guarantee for the availability of the downloaded data and the application also works without internet connectivity.

Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

## Build Instruction

Import the project into Android Studio, build and run the application.

## Attribution

This code was created by Martin Kacer, H21 lab, Copyright 2023.
https://www.h21lab.com/android


