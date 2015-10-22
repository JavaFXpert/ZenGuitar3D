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
 * InstConfig.fx - Part of a JavaFX 3D, and touch API, example that
 *                     uses David Koelle's JFugue5 library http://jfugue.org
 *
 *  Developed 2013 by James L. Weaver jim.weaver [at] javafxpert.com
 *  as a JavaFX 8 example for the Pro JavaFX 8 book.
 */
package projavafx.zenguitar3d.model;

public class InstConfig {
  private static int DEFAULT_NUM_STRINGS = 8;
  private static int CHINESE_GUITAR_NUM_STRINGS = 4;

  private static int DEFAULT_OPEN_NOTE_VALS[]
    = {46, 41, 37, 32, 27, 22, 17, 12};
  private static int CHINESE_GUITAR_OPEN_NOTE_VALS[]
    // = {34, 29, 25, 20};
    // = {51, 46, 44, 39, 34, 29, 24, 19};
      = {51, 46, 44, 39};

  public static InstConfig getInstConfigForMidi(int midiInstNum) {
    InstConfig instConfig;
    if (midiInstNum == 47) { //Koto (closest MIDI instrument?)
      instConfig =
        new InstConfig(CHINESE_GUITAR_NUM_STRINGS,
                       CHINESE_GUITAR_OPEN_NOTE_VALS,
                       true);
    }
    else {
      instConfig =
        new InstConfig(DEFAULT_NUM_STRINGS,
                       DEFAULT_OPEN_NOTE_VALS,
                       true);
    }
    return instConfig;
  }

  private int _numStrings;
  private int[] _openNoteVals;
  private boolean _bendEnabled;

  public InstConfig() {
    this(DEFAULT_NUM_STRINGS,
         DEFAULT_OPEN_NOTE_VALS,
         false);
  }

  public InstConfig(int numStrings,
                    int openNoteVals[],
                    boolean bendEnabled) {
    _numStrings = numStrings;
    _openNoteVals = openNoteVals;
    _bendEnabled = bendEnabled;
  }

  public int getNumStrings() {
    return _numStrings;
  }

  public int[] getOpenNoteVals() {
    return _openNoteVals;
  }

  public boolean isBendEnabled() {
    return _bendEnabled;
  }
}
