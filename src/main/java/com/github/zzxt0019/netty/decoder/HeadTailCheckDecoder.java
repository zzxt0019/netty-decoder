package com.github.zzxt0019.netty.decoder;

import com.github.zzxt0019.netty.transfer.BoolTransfer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * 头尾校验解码器
 */
public class HeadTailCheckDecoder extends HeadDecoder {
    /**
     * 上次尾帧位置
     */
    private static final AttributeKey<Integer> LAST_INDEX = AttributeKey.valueOf("com.github.zzxt0019.netty.LastIndex-" + attribute_key_count);
    /**
     * 尾标识
     */
    private final Byte[] tail;
    /**
     * 校验器
     */
    private final BoolTransfer checkTransfer;

    @Override
    protected boolean handle(ChannelHandlerContext ctx) throws Exception {
        if (!super.handle(ctx)) {
            return false;
        }
        ByteBuf buf = ctx.channel().attr(TEMP_BUF).get();
        ctx.channel().attr(LAST_INDEX).setIfAbsent(head.length);
        while (true) {
            int tailIndex = tailIndexOf(buf.slice(ctx.channel().attr(LAST_INDEX).get(), buf.readableBytes() - ctx.channel().attr(LAST_INDEX).get()));
            if (tailIndex == -1) {
                ctx.channel().attr(TEMP_BUF).set(buf);
                return false;
            }
            tailIndex += ctx.channel().attr(LAST_INDEX).get();
            if (checkTransfer.getFunction().apply(buf.slice(0, checkTransfer.getLength() > 0 ? checkTransfer.getLength() : tailIndex + tail.length + checkTransfer.getLength()))) {
                ctx.channel().attr(SEND_BUF).set(buf.slice(0, tailIndex + tail.length));
                ctx.channel().attr(TEMP_BUF).set(buf.slice(tailIndex + tail.length, buf.writerIndex() - tailIndex - tail.length).copy());
                ctx.channel().attr(LAST_INDEX).set(null);
                return true;
            } else {
                ctx.channel().attr(LAST_INDEX).set(tailIndex + 1);
            }
        }
    }

    /**
     * 查询尾表示位置
     *
     * @param buf 整体buf
     * @return 尾index
     */
    private int tailIndexOf(ByteBuf buf) {
        return indexOf(buf, tail);
    }

    private HeadTailCheckDecoder(Byte[] head, Byte[] tail, BoolTransfer checkTransfer, long maxTime, int maxLength) {
        super(head, maxTime, maxLength);
        this.tail = tail;
        this.checkTransfer = checkTransfer;
    }

    /**
     * 头尾校验解码器构造器
     *
     * @param head          头标识
     * @param tail          尾标识
     * @param checkTransfer 校验器
     *                      <pre>{@linkplain BoolTransfer checkTransfer}.{@linkplain Integer length}
     *                      <pre><b> > 0 ==></b> length
     *                      <pre><b> <=0 ==></b> buf.len + length
     *
     *                      @return {@link Builder} 构造器
     */
    public static Builder builder(Byte[] head, Byte[] tail, BoolTransfer checkTransfer) {
        return new Builder(head, tail, checkTransfer);
    }

    /**
     * 头尾校验解码器构造器
     *
     * @param head          头标识
     * @param tail          尾标识
     * @param checkTransfer 校验器
     *                      <pre>{@linkplain BoolTransfer checkTransfer}.{@linkplain Integer length}
     *                      <pre><b> > 0 ==></b> length
     *                      <pre><b> <=0 ==></b> buf.len + length
     *
     *                      @return {@link Builder} 构造器
     */
    public static Builder builder(byte[] head, Byte[] tail, BoolTransfer checkTransfer) {
        return new Builder(toByteArray(head), tail, checkTransfer);
    }

    /**
     * 头尾校验解码器构造器
     *
     * @param head          头标识
     * @param tail          尾标识
     * @param checkTransfer 校验器
     *                      <pre>{@linkplain BoolTransfer checkTransfer}.{@linkplain Integer length}
     *                      <pre><b> > 0 ==></b> length
     *                      <pre><b> <=0 ==></b> buf.len + length
     *
     *                      @return {@link Builder} 构造器
     */
    public static Builder builder(Byte[] head, byte[] tail, BoolTransfer checkTransfer) {
        return new Builder(head, toByteArray(tail), checkTransfer);
    }

    /**
     * 头尾校验解码器构造器
     *
     * @param head          头标识
     * @param tail          尾标识
     * @param checkTransfer 校验器
     *                      <pre>{@linkplain BoolTransfer checkTransfer}.{@linkplain Integer length}
     *                      <pre><b> > 0 ==></b> length
     *                      <pre><b> <=0 ==></b> buf.len + length
     *
     *                      @return {@link Builder} 构造器
     */
    public static Builder builder(byte[] head, byte[] tail, BoolTransfer checkTransfer) {
        return new Builder(toByteArray(head), toByteArray(tail), checkTransfer);
    }

    public static class Builder {
        private final Byte[] head;
        private final Byte[] tail;
        private final BoolTransfer checkTransfer;
        private long maxTime = DEFAULT_MAX_TIME;
        private int maxLength = DEFAULT_MAX_LENGTH;

        private Builder(Byte[] head, Byte[] tail, BoolTransfer checkTransfer) {
            this.head = head;
            this.tail = tail;
            this.checkTransfer = checkTransfer;
        }

        /**
         * 设置最大等待时间
         *
         * @param maxTime 最大等待时间(毫秒)
         * @return {@link Builder} 构造器
         */
        public Builder maxTime(long maxTime) {
            this.maxTime = maxTime;
            return this;
        }

        /**
         * 最大缓存长度
         *
         * @param maxLength 最大缓存长度(Byte)
         * @return {@link Builder} 构造器
         */
        public Builder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public HeadTailCheckDecoder build() {
            return new HeadTailCheckDecoder(head, tail, checkTransfer, maxTime, maxLength);
        }
    }
}
