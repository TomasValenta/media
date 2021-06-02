/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.exoplayer2.transformer;

import static org.junit.Assert.assertThrows;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.exoplayer2.util.MimeTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Unit test for {@link Transformer.Builder}. */
@RunWith(AndroidJUnit4.class)
@DoNotInstrument
public class TransformerBuilderTest {

  @Test
  public void setOutputMimeType_unsupportedMimeType_throws() {
    assertThrows(
        IllegalStateException.class,
        () -> new Transformer.Builder().setOutputMimeType(MimeTypes.VIDEO_FLV).build());
  }

  @Test
  public void build_withoutContext_throws() {
    assertThrows(IllegalStateException.class, () -> new Transformer.Builder().build());
  }

  @Test
  public void build_removeAudioAndVideo_throws() {
    Context context = ApplicationProvider.getApplicationContext();

    assertThrows(
        IllegalStateException.class,
        () ->
            new Transformer.Builder()
                .setContext(context)
                .setRemoveAudio(true)
                .setRemoveVideo(true)
                .build());
  }
}
