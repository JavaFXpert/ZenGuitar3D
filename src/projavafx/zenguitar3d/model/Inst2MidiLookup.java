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
 * Inst2MidiLookup.fx - Part of a JavaFX 3D, and touch API, example that
 *                      uses David Koelle's JFugue5 library http://jfugue.org
 *
 *  Developed 2013 by James L. Weaver jim.weaver [at] javafxpert.com
 *  as a JavaFX 8 example for the Pro JavaFX 8 book.
 */

package projavafx.zenguitar3d.model;

public class Inst2MidiLookup {
  // Midi instruments are 1 based
  private static int pickerIdx2midi[] = {
    46,  // Pizzicato Strings
    47,  // Orchestral Harp
    48,  // Timpani
    57,  // Trumpet
    58,  // Trombone
    61,  // French Horn
    67,  // Tenor Sax
    72,  // Clarinet
    23, // Harmonica 76,  // Pan Flute
    106, // Banjo
    1,   // Acoustic Grand Piano
    5,   // Electric Piano 1
    14,  // Xylophone
    22,  // Accordion
    25,  // Acoustic Guitar (nylon)
    31,  // Distortion Guitar
    33,  // Acoustic Bass
    41,  // Violin
    43,  // Cello
    47
    //108   // Koto
  };

  public static int getMidiByPickerIdx(int pickerIdx) {
    int midiNum = 0;
    if (pickerIdx >= 0 && pickerIdx < pickerIdx2midi.length) {
      midiNum = pickerIdx2midi[pickerIdx];
    }
    return midiNum;
  }
}
