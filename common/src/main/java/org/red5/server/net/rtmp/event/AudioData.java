/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2023 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package org.red5.server.net.rtmp.event;

import java.io.IOException;
import org.apache.mina.core.buffer.IoBuffer;
import org.red5.codec.AudioCodec;
import org.red5.io.ITag;

public class AudioData extends BaseStreamData<AudioData> {

  private static final long serialVersionUID = -4102940670913999407L;

  /** Audio codec */
  protected AudioCodec codec;

  /** True if this is configuration data and false otherwise */
  protected boolean config;

  /** Data type */
  private byte dataType = TYPE_AUDIO_DATA;

  /** Default constructor */
  public AudioData() {
    super(IoBuffer.allocate(0).flip());
  }

  /** Constructor with data buffer */
  public AudioData(IoBuffer data) {
    super(data);
  }

  /**
   * Create audio data event with given data buffer
   *
   * @param data Audio data
   * @param copy true to use a copy of the data or false to use reference
   */
  public AudioData(IoBuffer data, boolean copy) {
    super(data, copy);
  }

  /** {@inheritDoc} */
  @Override
  public byte getDataType() {
    return dataType;
  }

  /** Set the data type */
  public void setDataType(byte dataType) {
    this.dataType = dataType;
  }

  /** {@inheritDoc} */
  @Override
  public void setData(IoBuffer data) {
    super.setData(data);
    if (data != null && data.limit() > 0) {
      data.mark();
      codec = AudioCodec.valueOfById(((data.get(0) & 0xff) & ITag.MASK_SOUND_FORMAT) >> 4);
      // Determine by codec whether or not config data is included
      if (AudioCodec.getConfigured().contains(codec)) {
        config = (data.get() == 0);
      }
      data.reset();
    }
  }

  public void setData(byte[] data) {
    setData(IoBuffer.wrap(data));
  }

  public int getCodecId() {
    return codec.getId();
  }

  public boolean isConfig() {
    return config;
  }

  /** {@inheritDoc} */
  @Override
  public AudioData duplicate() throws IOException, ClassNotFoundException {
    AudioData result = super.duplicate();
    // Copy specific attributes for AudioData if necessary
    result.codec = this.codec;
    result.config = this.config;
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format(
        "Audio - ts: %s length: %s", getTimestamp(), (data != null ? data.limit() : '0'));
  }

  /** Create a new instance of AudioData */
  @Override
  protected AudioData createInstance() {
    return new AudioData();
  }
}
