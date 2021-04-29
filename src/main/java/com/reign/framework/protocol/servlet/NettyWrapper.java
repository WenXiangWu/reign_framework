package com.reign.framework.protocol.servlet;

import com.reign.framework.core.servlet.AbstractWrapper;
import com.reign.framework.core.util.WrapperUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

import java.util.Arrays;

/**
 * @ClassName: NettyWrapper
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 15:11
 **/
public class NettyWrapper extends AbstractWrapper {

    //buff分配器
    private ByteBufAllocator ALLOC;

    public NettyWrapper(boolean compress) {
        super(compress);
    }

    public NettyWrapper( ByteBufAllocator ALLOC,boolean compress) {
        super(compress);
        this.ALLOC = ALLOC;
    }

    @Override
    public Object wrapper(byte[] bytes) {
        ByteBuf buffer = ALLOC.heapBuffer(bytes.length);
        buffer.writeBytes(bytes);
        return buffer;
    }

    @Override
    public Object wrapper(String command, int requestId, byte[] body) {
        byte[] commandBytes = command.getBytes();
        //扩容到32
        commandBytes = Arrays.copyOf(commandBytes,32);
        //内容字节
        byte[] bodyBytes = wrapperBody(body);
        //包长度
        int dataLen = 36 + bodyBytes.length;
        ByteBuf buffer = ALLOC.heapBuffer(dataLen+4);
        buffer.writeInt(dataLen);
        buffer.writeBytes(commandBytes);
        buffer.writeInt(requestId);
        buffer.writeBytes(bodyBytes);
        return buffer;
    }

    @Override
    public Object wrapper(int requestId, byte[] body, boolean compress) {
        byte[] bodyBytes = wrapperBody(body);
        int dateLen = 4+bodyBytes.length;
        ByteBuf buffer = ALLOC.heapBuffer(dateLen+4);
        buffer.writeInt(dateLen);
        buffer.writeInt(requestId);
        buffer.writeBytes(bodyBytes);
        return buffer;
    }

    @Override
    public Object wrapper(int requestId, byte[] body) {
        byte[] bodyBytes = wrapperBody(body);
        int dateLen = 4+bodyBytes.length;
        ByteBuf buffer = ALLOC.heapBuffer(dateLen+4);
        buffer.writeInt(dateLen);
        buffer.writeInt(requestId);
        buffer.writeBytes(bodyBytes);
        return buffer;
    }

    @Override
    public Object wrapper(String command, int requestId, byte[] body, boolean compress) {
        byte[] commandBytes = command.getBytes();
        //扩容到32
        commandBytes = Arrays.copyOf(commandBytes,32);
        //内容字节
        byte[] bodyBytes = wrapperBody(body,compress);
        //包长度
        int dataLen = 36 + bodyBytes.length;
        ByteBuf buffer = ALLOC.heapBuffer(dataLen+4);
        buffer.writeInt(dataLen);
        buffer.writeBytes(commandBytes);
        buffer.writeInt(requestId);
        buffer.writeBytes(bodyBytes);
        return buffer;
    }

    @Override
    public Object wrapperWebSocket(String command, int requestId, byte[] body) {
        byte[] commandBytes = command.getBytes();
        //扩容到32
        commandBytes = Arrays.copyOf(commandBytes,32);
        //内容字节
        byte[] bodyBytes = wrapperBody(body);
        //包长度
        int dataLen = 36 + bodyBytes.length;
        ByteBuf buffer = ALLOC.heapBuffer(dataLen+4);
        buffer.writeInt(dataLen);
        buffer.writeBytes(commandBytes);
        buffer.writeInt(requestId);
        buffer.writeBytes(bodyBytes);
        return buffer;
    }



    @Override
    public Object wrapperChunk(byte[] chunk) {
        int len = chunk.length+4;
        String lenHex = Integer.toHexString(len);
        byte[] lenBytes = lenHex.getBytes();

        ByteBuf byteBuf = ALLOC.heapBuffer(len+lenBytes.length+(WrapperUtil.CRLF.length<<1));
        byteBuf.writeBytes(lenHex.getBytes()); //chunk size
        byteBuf.writeBytes(WrapperUtil.CRLF);
        byteBuf.writeInt(chunk.length);
        byteBuf.writeBytes(chunk);
        byteBuf.writeBytes(WrapperUtil.CRLF);
        return byteBuf;
    }

    @Override
    public void release(Object msg) {
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void retain(Object msg) {
        ReferenceCountUtil.retain(msg);
    }

    @Override
    public Object newWrapper(Object buffer) {
        ByteBuf buff = (ByteBuf)buffer;
        try{
            return Unpooled.copiedBuffer(buff);
        }finally {
            buff.release();
        }
    }

    @Override
    public Object getByteBufAllocator() {
        return ALLOC;
    }

    @Override
    public Object wrapperPush(String command, long playerId, byte[] body) {
        byte[] commandBytes = command.getBytes();
        //扩容到32
        commandBytes = Arrays.copyOf(commandBytes,32);
        //内容字节
        byte[] bodyBytes = wrapperBody(body,compress);
        //包长度
        int dataLen = 44 + bodyBytes.length;
        ByteBuf buffer = ALLOC.heapBuffer(dataLen+4);
        buffer.writeInt(dataLen);
        buffer.writeBytes(commandBytes);
        buffer.writeInt(0);
        buffer.writeBytes(bodyBytes);
        return buffer;
    }

    @Override
    public Object wrapperSocket(String command, int requestId, byte[] body) {
        byte[] commandBytes = command.getBytes();
        //扩容到32
        commandBytes = Arrays.copyOf(commandBytes,32);
        //内容字节
        byte[] bodyBytes = wrapperBody(body,compress);
        //包长度
        int dataLen = 36 + bodyBytes.length;
        ByteBuf buffer = ALLOC.heapBuffer(dataLen+4);
        buffer.writeInt(dataLen);
        buffer.writeBytes(commandBytes);
        buffer.writeInt(requestId);
        buffer.writeBytes(bodyBytes);
        return buffer;
    }
}
