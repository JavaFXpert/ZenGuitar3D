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
 * GuitarString3D.fx - Part of a JavaFX 3D, and touch API, example that
 *                     uses David Koelle's JFugue5 library http://jfugue.org
 *
 *  Developed 2013 by James L. Weaver jim.weaver [at] javafxpert.com
 *  as a JavaFX 8 example for the Pro JavaFX 8 book.
 */

package projavafx.zenguitar3d.ui;

import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.jfugue.pattern.Pattern;
import org.jfugue.realtime.RealTimePlayer;
import org.jfugue.theory.Note;

import javax.sound.midi.MidiUnavailableException;
import java.util.HashMap;

public class GuitarString3D extends Region {
  private RealTimePlayer _player;
  double _width;
  double _height;
  int _openNoteValue;
  int _numFrets;
  boolean _bendEnabled;

  Cylinder _stringCyl;
  Cylinder[] _fretCyls;
  DropShadow _dropShadowStringLine;

  // Most recent note value played
  //TODO: Remove?
  int _noteValue;

  // Y position of TouchPoint for most recent note played
  double _noteValuePosY;

  // Touchpoint IDs and corresponding note values
  HashMap _tpNoteVals;

  TranslateTransition _vibrateString;
  ZenGuitar3D _zenGuitar3D;

  public GuitarString3D(int openNoteValue, int numFrets,
                        double width, double height,
                        ZenGuitar3D zenGuitar3D) {
    _width = width;
    _height = height;
    _openNoteValue = openNoteValue;
    _numFrets = numFrets;
    _zenGuitar3D = zenGuitar3D;
    _tpNoteVals = new HashMap();

    try {
      _player = new RealTimePlayer();
    }
    catch (MidiUnavailableException e) {
      e.printStackTrace();
    }

    PhongMaterial fretMaterial =
      new PhongMaterial(Color.rgb(222, 215, 165));
    _fretCyls = new Cylinder[_numFrets + 1];
    for (int idx = 0; idx < _numFrets + 1; idx++) {
      _fretCyls[idx] = new Cylinder(3, _height);
      _fretCyls[idx].setTranslateX(idx * (_width / _numFrets));
      _fretCyls[idx].setTranslateY(_height / 2);
      _fretCyls[idx].setMaterial(fretMaterial);
    }

    // Make one fret bar thicker to indicate it is the nut
    _fretCyls[1].setRadius(6);

    _stringCyl = new Cylinder(5, width);
    _stringCyl.getTransforms().addAll(
      new Translate(_width / 2, _height / 2, -15),
      new Rotate(90, 0, 0, 0, Rotate.Z_AXIS)
    );
    _stringCyl.setMaterial(new PhongMaterial(Color.rgb(220, 220, 220)));

    _vibrateString = new TranslateTransition(new Duration(10), _stringCyl);
    _vibrateString.setByY(2);
    _vibrateString.setAutoReverse(true);
    _vibrateString.setCycleCount(Transition.INDEFINITE);

    setPrefSize(width, height);
    for (int idx = 0; idx < _numFrets + 1; idx++) {
      getChildren().add(_fretCyls[idx]);
    }
    getChildren().add(_stringCyl);

    setOnTouchPressed(te -> handleTouchPressed(te));
    setOnTouchReleased(te -> {
      if (!_zenGuitar3D.isMuteMode()) handleTouchReleased(te);
    });
    setOnTouchMoved(te -> {
      if (!_zenGuitar3D.isMuteMode()) handleTouchMoved(te);
    });
    setOnTouchStationary(te -> {
      if (!_zenGuitar3D.isMuteMode()) handleTouchMoved(te);
    });
  }

  @Override
  public void setPrefSize(double width, double height) {
    super.setPrefSize(width, height);
    _width = width;
    _height = height;
  }

  @Override
  public void setWidth(double width) {
    _width = width;
  }

  @Override
  public void setHeight(double height) {
    _height = height;
  }

  void handleTouchPressed(TouchEvent te) {
    if (!_zenGuitar3D.isMuteMode()) {
      if (isHighestTouchOnString(te)) {
        releaseAll(te);
        play(computeNoteValue(te.getTouchPoint().getX()), te.getTouchPoint(), false);
        _vibrateString.play();
      }
    }
    if (te.getTouchCount() >= 5) {
      _zenGuitar3D.showMidiPicker(false);
    }
    else if (numTouchPointsOnString(te) == 4) {
      _zenGuitar3D.setMuteMode(true);
      releaseAll(te);
    }
    else if (numTouchPointsOnString(te) == 3) {
      _zenGuitar3D.setMuteMode(false);
    }
  }

  void handleTouchReleased(TouchEvent te) {
    release(te.getTouchPoint());
    _vibrateString.pause();
  }

  void handleTouchMoved(TouchEvent te) {
    if (isHighestTouchOnString(te)) {
      int noteValue = computeNoteValue(te.getTouchPoint().getX());
      if (noteValue != _noteValue) {
        release(te.getTouchPoint());
        play(computeNoteValue(
             te.getTouchPoint().getX()), te.getTouchPoint(), true);
      }
      else {
        double bendDist =
          Math.abs((te.getTouchPoint().getY() - _noteValuePosY));
        double bendPct = bendDist / _height;
        if (_bendEnabled && bendPct > .25) {
          byte msb = (byte)Math.min(64 * (bendPct - .25), 64);
          _player.changePitchWheel((byte)0, msb);
        }
      }
    }
  }

  boolean isHighestTouchOnString(TouchEvent te) {
    double pointX = te.getTouchPoint().getX();
    double maxX = 0.0;
    for (TouchPoint tp : te.getTouchPoints()) {
      if (tp.belongsTo(this)) {
        maxX = Math.max(tp.getX(), maxX);
      }
    }
    return pointX >= maxX;
  }

  int numTouchPointsOnString(TouchEvent te) {
    int numTouchPoints = 0;
    for (TouchPoint tp : te.getTouchPoints()) {
      if (tp.belongsTo(this)) {
        numTouchPoints++;
      }
    }
    return numTouchPoints;
  }

  int computeNoteValue(double stringPosX) {
    //TODO: Change to logarithmic scale?
    double noteWidth = _width / _numFrets;
    double noteValue = stringPosX / noteWidth;
    int intNoteValue = new Double(noteValue).intValue() + 2;
    return _openNoteValue + intNoteValue;
  }

  private void play(int noteValue, TouchPoint tp,
                    boolean softAttack) {
    _tpNoteVals.put(tp.getId(),
                    new Integer(noteValue));
    _noteValue = noteValue;
    _noteValuePosY = tp.getY();
    Note note = new Note(noteValue);
    if (softAttack) {
      note.setAttackVelocity((byte)(note.getAttackVelocity() * 0.75));
    }
    Pattern pattern = note.getPattern();
    _player.play(pattern + "s-");
  }

  private void release(TouchPoint tp) {
    _vibrateString.pause();
    _player.changePitchWheel((byte)0, (byte)0);
    int noteValue = 0;
    Object nv = _tpNoteVals.get(tp.getId());
    if (nv != null) {
      noteValue = ((Integer)nv).intValue();
    }
    Pattern pattern = new Note(noteValue).getPattern();
    _player.play(pattern + "-s");
  }

  private void releaseAll(TouchEvent te) {
    for (TouchPoint tp : te.getTouchPoints()) {
      if (tp.belongsTo(this)) {
        release(tp);
      }
    }
  }

  public void setInstrument(int oneBasedInstNum) {
    _player.changeInstrument(oneBasedInstNum - 1);
  }

  public void setBendEnabled(boolean bendEnabled) {
    _bendEnabled = bendEnabled;
  }
}
