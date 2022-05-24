package com.github.zzxt0019.netty.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

/**
 * 缓存错误信息
 */
@ChannelHandler.Sharable
public abstract class CachedDecoder extends ChannelInboundHandlerAdapter {
    protected static int attribute_key_count = 0;
    /**
     * 发送的消息
     */
    protected final AttributeKey<ByteBuf> SEND_BUF = AttributeKey.valueOf("com.github.zzxt0019.netty.SendBuf-" + attribute_key_count);
    /**
     * 缓存的消息
     */
    protected final AttributeKey<ByteBuf> TEMP_BUF = AttributeKey.valueOf("com.github.zzxt0019.netty.TempBuf-" + attribute_key_count);
    /**
     * 上一次收到消息的时间
     */
    protected final AttributeKey<Long> LAST_TIME = AttributeKey.valueOf("com.github.zzxt0019.netty.LastTime-" + attribute_key_count);
    /**
     * 最大等待时间
     */
    protected final long maxTime;
    /**
     * 默认最大等待时间
     * <p>
     * 10s
     */
    protected static final long DEFAULT_MAX_TIME = 10000;
    /**
     * 最大缓存长度
     */
    protected final int maxLength;
    /**
     * 默认最大缓存长度
     * <p>
     * 20480Byte(20KB)
     */
    protected static final int DEFAULT_MAX_LENGTH = 20480;

    /**
     * 核心方法
     * <p>
     * 单次处理
     * <p>
     * 消息接收由TEMP_BUF
     * <p>
     * 消息发送至SEND_BUF
     *
     * @param ctx ctx
     * @return 是否通过
     * <p>
     * true 通过
     * <p>
     * false 不通过
     * <p>
     * 通过后会发送消息, 并继续处理
     * <p>
     * 如果想要继续处理但不发送消息, 将SEND_BUF设为null
     * <p>
     * 如果想要发送消息但不继续处理, 将TEMP_BUF设为null(一般没有这种情况)
     * @throws Exception ex
     */
    protected abstract boolean handle(ChannelHandlerContext ctx) throws Exception;

    /**
     * 解决时间和超长问题
     * <p>
     * 执行核心方法前判断时间是否舍弃旧缓存
     * <p>
     * 执行核心方法后判断长度是否舍弃旧缓存
     *
     * @param ctx ctx
     * @param buf 读到的数据
     */
    protected void handleMessage(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        try {
            if (ctx.channel().attr(LAST_TIME).getAndSet(System.currentTimeMillis()) - ctx.channel().attr(LAST_TIME).get() < -maxTime && ctx.channel().attr(TEMP_BUF).get() != null) {
                ctx.channel().attr(TEMP_BUF).set(null);
            }
            if (ctx.channel().attr(TEMP_BUF).get() == null) {
                ctx.channel().attr(TEMP_BUF).set(buf);
            } else {
                ctx.channel().attr(TEMP_BUF).set(ByteBufAllocator.DEFAULT.compositeBuffer().addComponents(true, ctx.channel().attr(TEMP_BUF).get(), buf));
            }
            while (handle(ctx)) {
                ByteBuf sendBuf = ctx.channel().attr(SEND_BUF).getAndSet(null);
                if (sendBuf != null) {
                    super.channelRead(ctx, sendBuf);
                }
            }
        } finally {
            if (ctx.channel().attr(TEMP_BUF).get() != null && ctx.channel().attr(TEMP_BUF).get().writerIndex() > maxLength) {
                ctx.channel().attr(TEMP_BUF).getAndSet(null).release();
            }
        }
    }

    /**
     * 入站时设置LAST_TIME
     *
     * @param ctx ctx
     * @throws Exception ex
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(LAST_TIME).set(0L);
        super.channelActive(ctx);
    }

    /**
     * 出站时清空BUF
     *
     * @param ctx ctx
     * @throws Exception ex
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().attr(TEMP_BUF).get() != null) {
            ctx.channel().attr(TEMP_BUF).getAndSet(null).release();
        }
        if (ctx.channel().attr(SEND_BUF).get() != null) {
            ctx.channel().attr(SEND_BUF).getAndSet(null).release();
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        handleMessage(ctx, (ByteBuf) msg);
    }

    /**
     * @param maxTime   最大等待时间(ms)
     * @param maxLength 最大缓存长度(Byte)
     */
    protected CachedDecoder(long maxTime, int maxLength) {
        this.maxTime = maxTime;
        this.maxLength = maxLength;
        attribute_key_count++;
    }
}
