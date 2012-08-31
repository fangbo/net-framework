package com.googlecode.server;

import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.googlecode.net.SelectorManager;

public class NioSelectorManager extends SelectorManager {

    Queue<SocketChannel> channelQueue = new ConcurrentLinkedQueue<SocketChannel>();

    public void addChannel(SocketChannel channel) {
        this.channelQueue.add(channel);
    }
    
    @Override
    protected void processEvent() {
        SocketChannel channel = null;
        while ((channel = this.channelQueue.poll()) != null) {
            try {
                channel.configureBlocking(false);
                channel.socket().setReuseAddress(true);
                AsyncRequestHandler handler = new AsyncRequestHandler(this.selector, channel);
                if (!isClosed.get()) {
                    channel.register(this.selector, SelectionKey.OP_READ, handler);
                }
            } catch (ClosedSelectorException e) {
                close();
                
                break;
            } catch (Exception e) {
                
            }
        }
    }

}
