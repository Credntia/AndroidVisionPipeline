[ ![Download](https://api.bintray.com/packages/iammehedi/Maven/online.devliving%3Amobilevisionpipeline/images/download.svg) ](https://bintray.com/iammehedi/Maven/online.devliving%3Amobilevisionpipeline/_latestVersion)

# AndroidVisionPipeline
The bare bone pipeline infrastructure required for using google's android vision detectors. Most of the source codes were extracted from [Google's android vision sample](https://github.com/googlesamples/android-vision).

## Pre-Requisite
Android Play Services SDK level 26 or greater.

## Setup
*Gradle*
```groovy
compile 'com.google.android.gms:play-services-basement:latest_version'
compile 'com.google.android.gms:play-services-vision:latest_version'
compile 'online.devliving:mobilevision-pipeline:latest_version'
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
Overlay graphics that you draw in the `GraphicOverlay` needs to extend the abstract `GraphicOverlay.Graphic` e.g `FaceGraphic` in the sample which draws a rectangle around a face that the `FaceDetector` has detected. 

To start the preview and detection you need to provide a `CameraSource` and a `Detector` 
```java
mPreview.start(mCameraSource, mGraphicOverlay);
```

## License
Copyright 2015 Google, Inc. All Rights Reserved.

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
