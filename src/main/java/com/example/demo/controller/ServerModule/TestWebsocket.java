/*
* @Author: Zhang Guohua
* @Date:   2019-01-21 13:31:10
* @Last Modified by:   zgh
* @Last Modified time: 2019-01-21 14:37:31
* @Description: create by zgh
* @GitHub: Savour Humor
*/
package com.example.demo.controller.ServerModule;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * 引子， 做服务的同事，指出我们建立的 图片流 websocket 不能及时关闭，总要等三五分钟才能关闭。即前端发出 close() 事件后三五分钟，后端的 websocket 断开。
 *  同事给出的几个说明：
 *      1. API 设计就是如此。
 *      2. 服务需要确定前端不再使用，才会关闭。大概三五分钟。
 *
 *
 * 我觉得这种理解是不符合 API 实现的原理的。即使我们需要双方确定，再进行关闭，也是即使发生的。
 *      1. API 设计者如果考虑到，也应该给出立即关闭的实现较为合理。
 *      2. 如果服务收到通知，进行处理，延迟解释是不合理的。
 *      3. 此处，如果解释为重连机制也不合理。
 *
 *  所以，我觉得是有待测试的：
 *      1. 首先，说用的 while true 循环。尝试后确实出现了无法关闭的问题。我觉得这里可能是由于同步执行的原理，程序无法空出时间执行 onClose 内容。
 *          后开启另外一个线程执行，可以达到瞬时关闭的效果。如果用同一个线程，则服务根本就接收不到 close 内容，故无法执行终止操作。
 *
 *       2.但是仍怀疑可能是重试的原因，因为如果同步，那么讲永远执行不出去，则无法停止，但是确实是过了几分钟终止了（是因为 sleep ？）。 即 判断 isOpen() 的状态在过了一会才发生改变。
 *          但只要执行 close ，就会立即 close。
 *          故 debug 尝试： 执行了 con.isOpen, 而是一直在推送消息，这个关闭应该是 websocket 自己的机制，即无人接收，过一会我就关了。所以这种监听方法也是错的。
 *
 *          感觉并没有及时收到 close 消息，至少这些方法里面没有，应该有专门的机制的。而同一个线程，导致了延迟接收。
 *
 *
 * 结论：
 *      1. 多线程，必然会实时关闭。
 *      2. 单线程，目前没有找到收到 close 立刻调用 的方法，如果有，在这个方法即可操作。
 *
 *
 * websocket 基本内容：
 *      1. 心跳链接，是为了防止 websocket 一分钟内，无消息会自动关闭。所以要告诉还活着。
 *
 */

public class TestWebsocket extends WebSocketServer {
    public TestWebsocket(int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    public TestWebsocket(InetSocketAddress address ) {
        super( address );
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake ) {
        conn.send("Welcome to the server!"); //This method sends a message to the new client
        broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This method sends a message to all clients connected
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        conn.closeConnection(code, "close");
        broadcast( conn + " has left the room!" );
        System.out.println( conn + " has left the room!" );
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        broadcast( message );

        while (!conn.isClosed()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println( conn + ": " + System.currentTimeMillis() );
        }
        /*thread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        thread.start();*/
        System.out.println();
    }
    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        broadcast( message.array() );
        System.out.println( conn + ": " + message );
    }


    public static void main( String[] args ) throws InterruptedException , IOException {
        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        TestWebsocket s = new TestWebsocket( port );
        s.start();
        System.out.println( "ChatServer started on port: " + s.getPort() );

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
            s.broadcast( in );
            if( in.equals( "exit" ) ) {
                s.stop(1000);
                break;
            }
        }
    }

    @Override
    public void onClosing(WebSocket conn, int code, String reason, boolean remote) {
        super.onClosing(conn, code, reason, remote);
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }
}
