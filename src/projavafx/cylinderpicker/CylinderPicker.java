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
 * CylinderPicker.fx - Part of a JavaFX 3D, and touch API, example that
 *                     uses David Koelle's JFugue5 library http://jfugue.org
 *
 *  Developed 2013 by James L. Weaver jim.weaver [at] javafxpert.com
 *  as a JavaFX 8 example for the Pro JavaFX 8 book.
 */

package projavafx.cylinderpicker;

import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class CylinderPicker extends Group {
  double _width;
  double _height;
  int _numFaces;
  double _faceWidth;
  double _faceAngle;

  Cylinder _cylinder;
  private double _curRotateAngle = 0.0;
  private int _curFace = 0;

  RotateTransition _rotCylTrans;

  ItemPick _itemCallback;
  DismissRequest _dismissCallback;

  public CylinderPicker(double width, double height,
                        int numFaces, int initialFace,
                        Image diffuseMap) {
    _width = width;
    _height = height;
    _curFace = initialFace;
    _numFaces = numFaces;
    _faceAngle = 360.0 / _numFaces;

    PhongMaterial mat = new PhongMaterial();
    mat.setDiffuseMap(diffuseMap);

    double radius = _width / 2;
    _faceWidth = 2 * radius * Math.sin(360 / (_numFaces * 2));
    _cylinder = new Cylinder (_width, _faceWidth);
    _cylinder.setMaterial(mat);

    _rotCylTrans =
      new RotateTransition(new Duration(1000), _cylinder);

    _cylinder.setRotationAxis(Rotate.Y_AXIS);

    _cylinder.setOnScrollStarted(e -> {
      _curRotateAngle = (_cylinder.getRotate() + 3600) % 360;
    });

    _cylinder.setOnScroll(e -> {
      if (!e.isInertia()) {
        _cylinder.setRotate(_curRotateAngle - e.getTotalDeltaX()
                            / 7);
      }
    });

    _cylinder.setOnTouchReleased(te -> {
      int facePicked = getFaceForAngle((_cylinder.getRotate() + 3600)
                                        % 360);
      rotateToFace(facePicked, false);
    });

    _cylinder.setOnTouchPressed(te -> {
      if (te.getTouchCount() >= 3) {
        _dismissCallback.dismissRequested(DismissRequest.OK);
      }
    });

    _cylinder.setOnMouseClicked(me -> {
      if (me.getClickCount() == 2) {
        Point3D pickedPoint =
          me.getPickResult().getIntersectedPoint();
        int facePicked = getFaceForPoint3D(pickedPoint);
        rotateToFace(facePicked, true);
      }
    });

    getChildren().add(_cylinder);

    setRotationAxis(Rotate.X_AXIS);
    setRotate(-8);
  }

  int getFaceForPoint3D(Point3D p3d) {
     double theta = Math.toDegrees(Math.atan2(p3d.getX(), p3d.getZ()));
     theta = (theta + 360) % 360;
     return getFaceForAngle(180 - theta);
  }

  int getFaceForAngle(double angle) {
    double dFace = (angle + 3600) % 360 / _faceAngle;
    int face = (int)Math.floor(dFace);
    return face;
  }

  public void rotateToFace(int face, boolean useTransition) {
    if (face != _curFace && _itemCallback != null) {
      _itemCallback.itemPicked(face);
    }
    double toAngle = (face + 0.5) * _faceAngle;
    if (useTransition) {
      _rotCylTrans.setToAngle(toAngle);
      _rotCylTrans.playFromStart();
    }
    else {
      _cylinder.setRotate(toAngle);
    }
    _curFace = face;
  }

  public int getCurFace() {
    return _curFace;
  }

  public void setOnItemPicked(ItemPick itemCallback) {
    _itemCallback = itemCallback;
  }

  public void setOnDismissRequested(DismissRequest dismissCallback) {
    _dismissCallback = dismissCallback;
  }
}
