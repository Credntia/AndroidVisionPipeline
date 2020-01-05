[![](https://jitpack.io/v/hannesa2/AndroidVisionPipeline.svg)](https://jitpack.io/#hannesa2/AndroidVisionPipeline)

# AndroidVisionPipeline
The bare bone pipeline infrastructure required for using google's android vision detectors. Most of the source codes were extracted from [Google's android vision sample](https://github.com/googlesamples/android-vision).

## Pre-Requisite
Android Play Services SDK level 26 or greater.

## Setup
*Gradle*
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
```groovy
implementation 'com.google.android.gms:play-services-basement:latest_version'
implementation 'com.google.android.gms:play-services-vision:latest_version'
implementation 'com.github.hannesa2:AndroidVisionPipeline:latest_version'
```

## Usage
You can add a camera preview with a graphic overlay (to draw stuff over the preview) in xml like this
```xml
<online.devliving.mobilevisionpipeline.camera.CameraSourcePreview
        android:id="@+id/preview"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <online.devliving.mobilevisionpipeline.GraphicOverlay
            android:id="@+id/faceOverlay"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

</online.devliving.mobilevisionpipeline.camera.CameraSourcePreview>
```
There are 2 scale types for the preview defined as 
```java
public enum PreviewScaleType{
    FIT_CENTER,
    FILL
}
```
for `FIT_CENTER` the preview will shrink (reduce it's width and height) to meet the aspect ratio of the camera's preview size.
for `FILL` the preview will scale up to fill (may be beyond it's view bounds) and meet the aspect ratio of the camera's preview size.

Overlay graphics that you draw in the `GraphicOverlay` needs to extend the abstract `GraphicOverlay.Graphic` e.g `FaceGraphic` in the sample which draws a rectangle around a face that the `FaceDetector` has detected. 

To start the preview and detection you need to provide a `CameraSource` (requires a `Detector` to process the frames) and a `GraphicOverlay` 
```java
mPreview.start(mCameraSource, mGraphicOverlay);
```

## License
Copyright 2015 Google, Inc. All Rights Reserved.

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
