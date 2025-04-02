package org.red5.server.net.rtmp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.red5.server.api.event.IEventDispatcher;
import org.red5.server.api.stream.IClientStream;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Ping;
import org.red5.server.net.rtmp.event.Unknown;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.net.rtmp.message.Header;
import org.red5.server.net.rtmp.message.Packet;
import org.red5.server.so.SharedObjectMessage;
import org.red5.server.net.ICommand;
import org.red5.server.net.rtmp.event.ChunkSize;

public class TestBaseRTMPHandler {

    private static class TestRTMPHandlerImpl extends BaseRTMPHandler {
        @Override
        protected void onChunkSize(RTMPConnection conn, Channel channel, Header source, ChunkSize chunkSize) {
        }

        @Override
        protected void onCommand(RTMPConnection conn, Channel channel, Header source, ICommand command) {
        }

        @Override
        protected void onPing(RTMPConnection conn, Channel channel, Header source, Ping ping) {
        }

        @Override
        protected void onSharedObject(RTMPConnection conn, Channel channel, Header source,
                SharedObjectMessage message) {
        }
    }

    @Test
    public void testMessageReceivedAudioData() throws Exception {
        RTMPConnection mockConn = mock(RTMPConnection.class);
        Channel mockChannel = mock(Channel.class);
        IClientStream mockStream = mock(IClientStream.class);
        IRTMPEvent mockEvent = mock(IRTMPEvent.class);

        Header header = new Header();
        header.setDataType(Constants.TYPE_AUDIO_DATA);
        header.setStreamId(1);
        header.setChannelId(5);

        Packet packet = new Packet(header, mockEvent);

        when(mockConn.getChannel(anyInt())).thenReturn(mockChannel);
        when(mockConn.getStreamById(any(Number.class))).thenReturn(mockStream);

        TestRTMPHandlerImpl handler = new TestRTMPHandlerImpl();

        handler.messageReceived(mockConn, packet);

        verify(mockEvent).setSourceType(Constants.SOURCE_TYPE_LIVE);
        verify((IEventDispatcher) mockStream).dispatchEvent(mockEvent);
        verify(mockEvent).release();
    }

    @Test
    public void testMessageReceivedUnknownType() throws Exception {
        RTMPConnection mockConn = mock(RTMPConnection.class);
        Channel mockChannel = mock(Channel.class);
        IRTMPEvent mockEvent = mock(IRTMPEvent.class);

        when(mockConn.getChannel(anyInt())).thenReturn(mockChannel);
        when(mockConn.getStreamById(any(Number.class))).thenReturn(null);

        Header header = new Header();
        byte unknownType = (byte) 99;
        header.setDataType(unknownType);
        header.setStreamId(1);
        header.setChannelId(5);
        Packet packet = new Packet(header, mockEvent);

        TestRTMPHandlerImpl handler = new TestRTMPHandlerImpl();
        handler.messageReceived(mockConn, packet);

        verify(mockEvent, never()).setSourceType(any());
        verify(mockEvent, never()).release();
    }
}
