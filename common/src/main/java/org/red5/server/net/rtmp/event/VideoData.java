/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2023 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package org.red5.server.net.rtmp.event;

import java.io.IOException;
import org.apache.mina.core.buffer.IoBuffer;
import org.red5.codec.VideoCodec;
import org.red5.io.ITag;
import org.red5.io.IoConstants;

/** Video data event */
public class VideoData extends BaseStreamData<VideoData> implements IoConstants {

  private static final long serialVersionUID = 5538859593815804830L;

  /** Videoframe type */
  public static enum FrameType {
    UNKNOWN,
    KEYFRAME,
    INTERFRAME,
    DISPOSABLE_INTERFRAME,
    END_OF_SEQUENCE
  }

  /** Frame type, unknown by default */
  protected FrameType frameType = FrameType.UNKNOWN;

  /** Video codec */
  protected VideoCodec codec;

  /** True if this is configuration data and false otherwise */
  protected boolean config;

  /** True if this indicates an end-of-sequence and false otherwise */
  protected boolean endOfSequence;

  /** Constructs a new VideoData. */
  public VideoData() {
    super(IoBuffer.allocate(0).flip());
  }

  /**
   * Create video data event with given data buffer
   *
   * @param data Video data
   */
  public VideoData(IoBuffer data) {
    super(data);
  }

  /**
   * Create video data event with given data buffer
   *
   * @param data Video data
   * @param copy true to use a copy of the data or false to use reference
   */
  public VideoData(IoBuffer data, boolean copy) {
    super(data, copy);
  }

  /** {@inheritDoc} */
  @Override
  public byte getDataType() {
    return dataType;
  }

  public void setDataType(byte dataType) {
    this.dataType = dataType;
  }

  /** {@inheritDoc} */
  @Override
  public void setData(IoBuffer data) {
    super.setData(data);
    if (data != null && data.limit() > 0) {
      data.mark();
      int firstByte = data.get(0) & 0xff;
      codec = VideoCodec.valueOfById(firstByte & ITag.MASK_VIDEO_CODEC);
      // determine by codec whether or not frame / sequence types are included
      if (VideoCodec.getConfigured().contains(codec)) {
        int secondByte = data.get(1) & 0xff;
        config = (secondByte == 0);
        endOfSequence = (secondByte == 2);
      }
      data.reset();
      int frameType = (firstByte & MASK_VIDEO_FRAMETYPE) >> 4;
      if (frameType == FLAG_FRAMETYPE_KEYFRAME) {
        this.frameType = FrameType.KEYFRAME;
      } else if (frameType == FLAG_FRAMETYPE_INTERFRAME) {
        this.frameType = FrameType.INTERFRAME;
      } else if (frameType == FLAG_FRAMETYPE_DISPOSABLE) {
        this.frameType = FrameType.DISPOSABLE_INTERFRAME;
      } else {
        this.frameType = FrameType.UNKNOWN;
      }
    }
  }

  public void setData(byte[] data) {
    setData(IoBuffer.wrap(data));
  }

  /**
   * Getter for frame type
   *
   * @return Type of video frame
   */
  public FrameType getFrameType() {
    return frameType;
  }

  public int getCodecId() {
    return codec.getId();
  }

  public boolean isConfig() {
    return config;
  }

  public boolean isEndOfSequence() {
    return endOfSequence;
  }

  /** {@inheritDoc} */
  @Override
  protected void releaseInternal() {
    if (data != null) {
      final IoBuffer localData = data;
      // null out the data first so we don't accidentally
      // return a valid reference first
      data = null;
      localData.clear();
      localData.free();
    }
  }

  /**
   * Duplicate this message / event.
   *
   * @return duplicated event
   */
  @Override
  public VideoData duplicate() throws IOException, ClassNotFoundException {
    VideoData result = super.duplicate();
    result.frameType = this.frameType;
    result.codec = this.codec;
    result.config = this.config;
    result.endOfSequence = this.endOfSequence;
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format(
        "VideoData - ts: %s length: %s", getTimestamp(), (data != null ? data.limit() : '0'));
  }

  /** Create a new instance of VideoData */
  @Override
  protected VideoData createInstance() {
    return new VideoData();
  }
}
