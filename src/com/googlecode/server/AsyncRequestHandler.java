package com.googlecode.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;

import com.googlecode.net.SelectorManagerWorker;

public class AsyncRequestHandler extends SelectorManagerWorker {

    public AsyncRequestHandler(Selector selector, SocketChannel clientChannel) {
        super(selector, clientChannel);
    }

    @Override
    protected void doRead(SelectionKey key) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int reads = 0;
        while ((reads = channel.read(buffer)) > 0) {
            buffer.flip();
            out.write(buffer.array(), 0, buffer.capacity() - buffer.remaining());
            System.out.println("buffer read size: " + (buffer.capacity() - buffer.remaining()));
            buffer.clear();
        }
        System.out.println(out.toByteArray().length);
        String result = new String(out.toByteArray());
        System.out.println(result.length());
        System.out.println(result);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    protected void doWrite(SelectionKey key) throws IOException {
        String json = "Hello World";
        String HEAD = "HTTP/1.0 200 OK\r\n" 
            + "Date: " + new Date() 
            + "\r\nContent-Length: " + json.getBytes("ASCII").length 
            + "\r\nContent-Type: text/plain\r\n\r\n";
        
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(ByteBuffer.wrap((HEAD + json).getBytes()));
        key.interestOps(0);
    }

}
