package io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Chandler on 10/12/16.
 */
public class NIO2EchoServer {
    public void serve(int port) throws IOException {
        System.out.println(port);
        final AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(port);
        serverSocketChannel.bind(address);
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        //Start to accept new Client connections. Once one is accepted the CompletionHandler will get called.
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, Object attachment) {
                serverSocketChannel.accept(null, this);
                final ByteBuffer buffer = ByteBuffer.allocate(100);
                //Trigger a read operation on the Channel,
                //the given CompletionHandler will be notified once something was read.
                channel.read(buffer, buffer, new EchoCompletionHandler(channel));
            }

            @Override
            public void failed(Throwable throwable, Object attachment) {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    System.out.println(e);
                } finally {
                    countDownLatch.countDown();
                }
            }
        });
    }
}
