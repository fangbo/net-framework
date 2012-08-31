package com.googlecode.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketService {
    
    private int acceptNum = 1;
    
    private AtomicBoolean isClosed = new AtomicBoolean(false);
    
    private int selectorNum;
    private NioSelectorManager[] selectorManagers = null;
    private ExecutorService selectorPool = null; 
    
    private int port;
    
    private ServerSocketChannel serverChannel;

    public SocketService(int port, int selectorNum) throws IOException {
        this.port = port;
        this.selectorNum = selectorNum;
        
        startInner();
    }
    
    private void startInner() throws IOException{
        this.selectorManagers = new NioSelectorManager[this.selectorNum];
        this.selectorPool = Executors.newFixedThreadPool(this.selectorNum);
        for (int i = 0; i < this.selectorNum; i++) {
            NioSelectorManager manager = new NioSelectorManager();
            this.selectorManagers[i] = manager;
            this.selectorPool.submit(manager);
        }
        
        serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(this.port));
        
        // acceptor thread
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        SocketChannel socketChannel = serverChannel.accept();
                        accept(socketChannel);
                    } catch (ClosedChannelException e) {
                        close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    
    public void close() {
        if (!isClosed.compareAndSet(false, true)) {
            return;
        }
        
        for (NioSelectorManager manager : this.selectorManagers) {
            manager.close();
        }
        
        try {
            this.serverChannel.socket().close();
        } catch (IOException e) {
            
        }
        
        try {
            this.serverChannel.close();
        } catch (IOException e) {
            
        }
        
    }
    
    private void accept(SocketChannel channel) {
        int managerIndex = (this.acceptNum++) % this.selectorNum;
        this.selectorManagers[managerIndex].addChannel(channel);
    }
    
    
    public static void main(String[] args) throws IOException{
        SocketService service = new SocketService(47865, 3);
    }
}
