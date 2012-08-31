package com.googlecode.net;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.googlecode.FrameworkException;

public abstract class SelectorManager implements Runnable{

    private static final long SELECTOR_POLL_TIMEOUT = 200;
    
    protected AtomicBoolean isClosed = new AtomicBoolean(false);

    protected Selector selector;
    
    private static final Logger logger = Logger.getLogger(SelectorManager.class);
    
    public SelectorManager() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new FrameworkException("can not open new selector", e);
        }
    }
    
    public void close() {
        if (!isClosed.compareAndSet(false, true)) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn("selector has already been closed!");
            }
            
            return;
        }
        
        try {
            for (SelectionKey key : selector.keys()) {
                try {
                    key.channel().close();
                } catch (Exception e) {
                    if (logger.isEnabledFor(Level.WARN)) {
                        logger.warn(e.getMessage(), e);
                    }
                }
                
                try {
                    key.cancel();
                } catch (Exception e) {
                    if (logger.isEnabledFor(Level.WARN)) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            if (logger.isEnabledFor(Level.ERROR)) {
                logger.error(e.getMessage(), e);
            }
        }
        
        try {
            selector.close();
        } catch (IOException e) {
            if (logger.isEnabledFor(Level.ERROR)) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    protected abstract void processEvent();

    public void run() {
        try {
            while (true) {
                if (isClosed.get()) {
                    if (logger.isEnabledFor(Level.INFO)) {
                        logger.info("selector closed, exist!");
                    }
                    break;
                }
                
                processEvent();
                
                try {
                    int selected = selector.select(SELECTOR_POLL_TIMEOUT);
                    
                    if (isClosed.get()) {
                        if (logger.isEnabledFor(Level.INFO)) {
                            logger.info("selector closed, exist!");
                        }
                        break;
                    }
                    
                    if (selected == 0) {
                        continue;
                    }
                    
                    Iterator<SelectionKey> keysItr = selector.selectedKeys().iterator();
                    while (keysItr.hasNext()) {
                        SelectionKey key = keysItr.next();
                        keysItr.remove();
                        
                        if (key.isReadable() || key.isWritable()) {
                            Runnable worker = (Runnable)key.attachment();
                            worker.run();
                        }
                    }
                } catch (ClosedSelectorException e) {
                    if (logger.isEnabledFor(Level.ERROR)) {
                        logger.error("selector is closed!", e);
                    }
                    break;
                } catch (Throwable e) {
                    if (logger.isEnabledFor(Level.ERROR)) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            if (logger.isEnabledFor(Level.ERROR)) {
                logger.error(e.getMessage(), e);
            }
        } finally {
            try {
                close();
            } catch (Exception e) {
                if (logger.isEnabledFor(Level.ERROR)) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
