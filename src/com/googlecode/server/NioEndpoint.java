package com.googlecode.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.googlecode.net.SelectorManagerWorker;

public class NioEndpoint extends SelectorManagerWorker {

    private ByteBuffer reqBuffer;
    
    private ByteBuffer respBuffer;
    
    private static final Logger logger = Logger.getLogger(NioEndpoint.class);
    
    public NioEndpoint(Selector selector, SocketChannel clientChannel, int bufferSize) {
        super(selector, clientChannel);
        
        reqBuffer = ByteBuffer.allocate(bufferSize);
        respBuffer = ByteBuffer.allocate(bufferSize);
    }

    protected void doRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel)key.channel();
        channel.read(reqBuffer);
    }

    protected void doWrite(SelectionKey key) throws IOException {

    }
    
    
    public void finishResponse() {
        try {
            this.clientChannel.keyFor(this.selector).interestOps(SelectionKey.OP_READ);
        } catch (Exception e) {
            logger.error("fail to finish response, close channel", e);
            close();
        }
    }
}
