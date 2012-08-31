package com.googlecode.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public abstract class SelectorManagerWorker implements Runnable {

    protected Selector selector;
    
    protected SocketChannel clientChannel;
    
    private AtomicBoolean isClosed = new AtomicBoolean(false);

    private static final Logger logger = Logger.getLogger(SelectorManagerWorker.class);
    
    public SelectorManagerWorker(Selector selector, SocketChannel clientChannel) {
        this.selector = selector;
        this.clientChannel = clientChannel;
    }
    
    protected abstract void doRead(SelectionKey key) throws IOException;
    
    protected abstract void doWrite(SelectionKey key) throws IOException;
    
    public void run() {
        try {
            SelectionKey key = this.clientChannel.keyFor(this.selector);
            if (key.isReadable()) {
                this.doRead(key);
            } else if (key.isWritable()) {
                this.doWrite(key);
            } else if (!key.isValid()) {
                throw new IllegalStateException("selection key is invalid!");
            }
        } catch (Throwable e) {
            logger.error("error in channel", e);
            close();
        }
    }
    
    public void close() {
        if (!isClosed.compareAndSet(false, true)) {
            if (logger.isEnabledFor(Level.INFO)) {
                logger.info("already closed!");
            }
            return;
        }
        
        try {
            this.clientChannel.keyFor(this.selector).cancel();
        } catch (Exception e) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn(e.getMessage(), e);
            }
        }
        
        try {
            this.clientChannel.close();
        } catch (IOException e) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

}
