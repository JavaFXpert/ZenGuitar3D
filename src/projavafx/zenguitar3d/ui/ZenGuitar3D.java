/*
 * Copyright (c) 2013, Pro JavaFX Authors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of JFXtras nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * ZenGuitar3D.fx - A JavaFX 3D, and touch API, example that
 *                  uses David Koelle's JFugue5 library http://jfugue.org
 *
 *  Developed 2013 by James L. Weaver jim.weaver [at] javafxpert.com
 *  as a JavaFX 8 example for the Pro JavaFX 8 book.
 */

package projavafx.zenguitar3d.ui;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import projavafx.cylinderpicker.CylinderPicker;
import projavafx.zenguitar3d.model.Inst2MidiLookup;
import projavafx.zenguitar3d.model.InstConfig;

public class ZenGuitar3D extends Application {
  static int INITIAL_INST_PICKER_FACE = 14;
  static double NECK_HEIGHT = 900;

  static int LOW_NOTE = 18;
  static int NUM_FRETS = 15;
  static double STRING_WIDTH = 1900;
  static double NECK_DEPTH = 100;
  static double GUITAR_INITIAL_Z = 40;

  static int _numStrings;
  static double _stringHeight;
  boolean _muteMode = false;
  double _curZoomFactor = 1.0;
  Group _guitar;
  Box _neck;
  PhongMaterial _markerMaterial;

  DoubleProperty _guitarAngleX = new SimpleDoubleProperty(0);
  DoubleProperty _guitarAngleY = new SimpleDoubleProperty(0);
  DoubleProperty _guitarAngleZ = new SimpleDoubleProperty(0);

  Rotate _guitarRotateX;
  Rotate _guitarRotateY;
  Rotate _guitarRotateZ;

  Timeline _goHomeAnim;
  CylinderPicker _midiPicker;
  TranslateTransition _showMidiPicker;
  TranslateTransition _hideMidiPicker;
  SequentialTransition _showHideMidiPicker;

  VBox _guitarStringsContainer;

  PerspectiveCamera scenePerspectiveCamera =
    new PerspectiveCamera(false);

  public static void main(String[] args) {
    System.setProperty("prism.dirtyopts", "false");
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) {
    Image zgDiffuseMap =
        new Image(ZenGuitar3D.class
            .getResource("wood.jpeg")
            .toExternalForm());

    PhongMaterial woodMaterial = new PhongMaterial();
    woodMaterial.setDiffuseMap(zgDiffuseMap);

    _neck = new Box(STRING_WIDTH, NECK_HEIGHT, NECK_DEPTH);
    _neck.setMaterial(woodMaterial);
    _neck.setTranslateZ(NECK_DEPTH / 2);

    final Group root = new Group();
    _guitar = new Group();
    root.getChildren().add(_guitar);

    _guitarStringsContainer = new VBox();
    _guitarStringsContainer.setSpacing(0);

    _guitarStringsContainer.setTranslateX(-STRING_WIDTH / 2);
    _guitarStringsContainer.setTranslateY(-NECK_HEIGHT / 2);
    _guitarStringsContainer.setTranslateZ(0);

    _goHomeAnim = new Timeline(
      new KeyFrame(
        new Duration(1000),
        new KeyValue(_guitarAngleX, 0),
        new KeyValue(_guitarAngleY, 0),
        new KeyValue(_guitarAngleZ, 0),
        new KeyValue(_guitar.scaleXProperty(), 1.0),
        new KeyValue(_guitar.scaleYProperty(), 1.0),
        new KeyValue(_guitar.scaleZProperty(), 1.0),
        new KeyValue(_guitar.translateXProperty(),
                                  STRING_WIDTH / 2),
        new KeyValue(_guitar.translateYProperty(),
                                   NECK_HEIGHT / 2),
        new KeyValue(_guitar.translateZProperty(),
                                  GUITAR_INITIAL_Z)
      )
    );

    _markerMaterial = new PhongMaterial(Color.rgb(222, 215, 165));
    double fretWidth = STRING_WIDTH / NUM_FRETS;

    _guitar.setTranslateX(STRING_WIDTH / 2);
    _guitar.setTranslateY(NECK_HEIGHT / 2);
    _guitar.setTranslateZ(GUITAR_INITIAL_Z);

    _guitar.getTransforms().setAll(
      _guitarRotateX = new Rotate(0, Rotate.X_AXIS),
      _guitarRotateY = new Rotate(0, Rotate.Y_AXIS),
      _guitarRotateZ = new Rotate(0, Rotate.Z_AXIS)
    );

    _guitarRotateX.angleProperty().bind(_guitarAngleX);
    _guitarRotateY.angleProperty().bind(_guitarAngleY);
    _guitarRotateZ.angleProperty().bind(_guitarAngleZ);

    createFretboard(Inst2MidiLookup
      .getMidiByPickerIdx(INITIAL_INST_PICKER_FACE));

    Image cpDiffuseMap =
      new Image(ZenGuitar3D.class
        .getResource("20-instruments-w-pipa.png")
        .toExternalForm());

    _midiPicker = new CylinderPicker(
      300, 300, 20,
      INITIAL_INST_PICKER_FACE,
      cpDiffuseMap
    );
    _midiPicker.setTranslateZ(400);
    _midiPicker.setOnItemPicked((int ip) -> {
      int midiInstNum = Inst2MidiLookup.getMidiByPickerIdx(ip);
      setStringsToInstrument(midiInstNum);
    });
    _midiPicker.setOnDismissRequested(dismissType -> {
      _hideMidiPicker.play();
    });

    _showMidiPicker =
      new TranslateTransition(
        new Duration(2000),
        _midiPicker
      );
    _showMidiPicker.setFromZ(400);
    _showMidiPicker.setToZ(-100);

    _hideMidiPicker =
      new TranslateTransition(
        new Duration(1000),
        _midiPicker
      );
    _hideMidiPicker.setFromZ(-100);
    _hideMidiPicker.setToZ(400);

    _showHideMidiPicker = new SequentialTransition();

    _guitar.getChildren()
      .setAll(
          _neck, _guitarStringsContainer,
          createFretMarker(0, _markerMaterial),
          createFretMarker(1, _markerMaterial),
          createFretMarker(2, _markerMaterial),
          createFretMarker(3, _markerMaterial),
          createFretMarker(4, _markerMaterial),
          createFretMarker(5, _markerMaterial),
          _midiPicker
      );

    _guitar.setOnZoomStarted(e -> {
      if (_muteMode) {
        _curZoomFactor = _guitar.getScaleX();
      }
    });

    _guitar.setOnZoom(e -> {
      if (_muteMode) {
        _guitar.setScaleX(e.getTotalZoomFactor() * _curZoomFactor);
        _guitar.setScaleY(e.getTotalZoomFactor() * _curZoomFactor);
        _guitar.setScaleZ(e.getTotalZoomFactor() * _curZoomFactor);
        if (_guitar.getScaleX() < .25) {
          goHomePos();
        }
      }
    });

    _guitar.setOnScroll(e -> {
      if (_muteMode) {
        if (!e.isInertia()) {
          _guitarAngleX.set(_guitarAngleX.get() + e.getDeltaY() / 3);
          _guitarAngleY.set(_guitarAngleY.get() - e.getDeltaX() / 3);
        }
      }
    });

    _guitar.setOnRotate(e -> {
      if (_muteMode) {
        _guitarAngleZ.set(_guitarAngleZ.get() + e.getAngle());
      }
    });

    final Scene scene =
      new Scene(root, STRING_WIDTH, NECK_HEIGHT, true);
    scene.setFill(Color.WHITE);

    PointLight pointLight = new PointLight(Color.WHITE);
    pointLight.setTranslateX(STRING_WIDTH * .33);
    pointLight.setTranslateY(NECK_HEIGHT * .33);
    pointLight.setTranslateZ(-2000);

    scene.setCamera(scenePerspectiveCamera);

    root.getChildren().addAll(pointLight, scenePerspectiveCamera);

    stage.setScene(scene);
    stage.setTitle("ZenGuitar3D");
    stage.show();

    showMidiPicker(true);
  }

  void showMidiPicker(boolean hideAfterShow) {
    if (hideAfterShow) {
      _showHideMidiPicker.getChildren().setAll(_showMidiPicker,
                                               _hideMidiPicker);
    }
    else {
      _showHideMidiPicker.getChildren().setAll(_showMidiPicker);
    }
    _showHideMidiPicker.playFromStart();
    _midiPicker.rotateToFace(_midiPicker.getCurFace(), true);
  }

  Cylinder createFretMarker(int markerNum, PhongMaterial mat) {
    double fretWidth = STRING_WIDTH / NUM_FRETS;
    int fretNum = 0;
    boolean topMarker = false;
    boolean bottomMarker = false;
    Cylinder fretMarker =
      new Cylinder(STRING_WIDTH / NUM_FRETS / 3, 1);
    fretMarker.setMaterial(mat);
    fretMarker.setRotationAxis(Rotate.X_AXIS);
    fretMarker.setRotate(90);
    switch (markerNum) {
      case 0:
        fretNum = 3;
        break;
      case 1:
        fretNum = 5;
        break;
      case 2:
        fretNum = 7;
        break;
      case 3:
        fretNum = 9;
        break;
      case 4:
        fretNum = 12;
        topMarker = true;
        break;
      case 5:
        fretNum = 12;
        bottomMarker = true;
        break;
    }
    fretMarker.setTranslateX(fretWidth * (fretNum + 0.5)
                             - STRING_WIDTH / 2);
    if (topMarker) {
      fretMarker.setTranslateY(-NECK_HEIGHT / _numStrings);
    }
    else if(bottomMarker) {
      fretMarker.setTranslateY(NECK_HEIGHT / _numStrings);
    }
    return fretMarker;
  }

  void createFretboard(int midiInstNum) {
    InstConfig instConfig =
      InstConfig.getInstConfigForMidi(midiInstNum);
    _numStrings = instConfig.getNumStrings();
    int openNoteVals[] = instConfig.getOpenNoteVals();
    _stringHeight = NECK_HEIGHT / _numStrings;
    _guitarStringsContainer.getChildren().removeAll();
    for (int idx = 0; idx < _numStrings; idx++) {
      _guitarStringsContainer.getChildren().add(
        new GuitarString3D(
          LOW_NOTE + openNoteVals[idx],
          NUM_FRETS,
          STRING_WIDTH,
          _stringHeight,
          this)
      );
    }
    setStringsToInstrument(midiInstNum);
    setStringsToBend(instConfig.isBendEnabled());
  }

  void setMuteMode(boolean muteMode) {
    _muteMode = muteMode;
  }

  boolean isMuteMode() {
    return _muteMode;
  }

  void goHomePos() {
    setMuteMode(false);
    _goHomeAnim.playFromStart();
  }

  void setStringsToInstrument(int midiInstNum) {
    for (Node guitarString3D :
        _guitarStringsContainer.getChildren()) {
      ((GuitarString3D)guitarString3D).setInstrument(midiInstNum);
    }
  }

  void setStringsToBend(boolean bendEnabled) {
    for (Node guitarString3D :
        _guitarStringsContainer.getChildren()) {
      ((GuitarString3D)guitarString3D).setBendEnabled(bendEnabled);
    }
  }
}
