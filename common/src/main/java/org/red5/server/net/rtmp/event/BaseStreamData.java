package org.red5.server.net.rtmp.event;

import java.io.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.stream.IStreamData;

public abstract class BaseStreamData<T extends BaseStreamData<T>> extends BaseEvent
    implements IStreamData<T>, IStreamPacket {

  private static final long serialVersionUID = 1L;

  /** Data buffer */
  protected IoBuffer data;

  /** Data type (to be set by the subclass) */
  protected byte dataType;

  /** Constructs a new BaseStreamData with an empty buffer */
  public BaseStreamData() {
    this(IoBuffer.allocate(0).flip());
  }

  /** Constructs a new BaseStreamData with the given data */
  public BaseStreamData(IoBuffer data) {
    super(Type.STREAM_DATA);
    setData(data);
  }

  /** Constructs a new BaseStreamData with the given data and a flag to copy or not */
  public BaseStreamData(IoBuffer data, boolean copy) {
    super(Type.STREAM_DATA);
    if (copy) {
      byte[] array = new byte[data.remaining()];
      data.mark();
      data.get(array);
      data.reset();
      setData(array);
    } else {
      setData(data);
    }
  }

  /** {@inheritDoc} */
  public IoBuffer getData() {
    return data;
  }

  /** Set the data */
  public void setData(IoBuffer data) {
    this.data = data;
  }

  /** Set the data from a byte array */
  public void setData(byte[] data) {
    setData(IoBuffer.wrap(data));
  }

  /** {@inheritDoc} */
  @Override
  protected void releaseInternal() {
    if (data != null) {
      final IoBuffer localData = data;
      data = null;
      localData.clear();
      localData.free();
    }
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    byte[] byteBuf = (byte[]) in.readObject();
    if (byteBuf != null) {
      setData(byteBuf);
    }
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    if (data != null) {
      if (data.hasArray()) {
        out.writeObject(data.array());
      } else {
        byte[] array = new byte[data.remaining()];
        data.mark();
        data.get(array);
        data.reset();
        out.writeObject(array);
      }
    } else {
      out.writeObject(null);
    }
  }

  /**
   * Duplicate this message / event.
   *
   * @return duplicated event
   */
  public T duplicate() throws IOException, ClassNotFoundException {
    T result = createInstance();
    // serialize
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    writeExternal(oos);
    oos.close();
    // convert to byte array
    byte[] buf = baos.toByteArray();
    baos.close();
    // create input streams
    ByteArrayInputStream bais = new ByteArrayInputStream(buf);
    ObjectInputStream ois = new ObjectInputStream(bais);
    // deserialize
    result.readExternal(ois);
    ois.close();
    bais.close();
    // clone the header if there is one
    if (header != null) {
      result.setHeader(header.clone());
    }
    result.setSourceType(sourceType);
    result.setSource(source);
    result.setTimestamp(timestamp);
    return result;
  }

  /** Create a new instance of the subclass */
  protected abstract T createInstance();

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format(
        "%s - ts: %s length: %s",
        getClass().getSimpleName(), getTimestamp(), (data != null ? data.limit() : '0'));
  }
}
