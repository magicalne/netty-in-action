package io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by Chandler on 10/12/16.
 */
public class EchoCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
    private final AsynchronousSocketChannel channel;

    public EchoCompletionHandler(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void completed(Integer result, ByteBuffer byteBuffer) {
        byteBuffer.flip();
//        Trigger a write operation on the Channel, the given CompletionHandler will be notified once
//        something was written
        channel.write(byteBuffer, byteBuffer, new CompletionHandler<Integer,
                ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if (buffer.hasRemaining()) {
                    // Trigger again a write operation if something is left in the ByteBuffer
                    channel.write(buffer, buffer, this);
                } else {
                    buffer.compact();
                    //Trigger a read operation on the Channel, the given CompletionHandler will be notified once
                    //something was read
                    channel.read(buffer, buffer,
                            EchoCompletionHandler.this);
                }
            }
            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    channel.close();
                } catch (IOException e) {
                    // ingnore on close
                }
            }
        });
    }

    @Override
    public void failed(Throwable exc, ByteBuffer byteBuffer) {
        try {
            channel.close();
        } catch (IOException e) {
            // ingnore on close
        }
    }
}
