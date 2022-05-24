package com.github.zzxt0019.netty.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 头尾解码器
 */
public class HeadTailDecoder extends HeadDecoder {
    /**
     * 尾标识
     */
    private final Byte[] tail;


    @Override
    protected boolean handle(ChannelHandlerContext ctx) throws Exception {
        if (!super.handle(ctx)) {
            return false;
        }
        ByteBuf buf = ctx.channel().attr(TEMP_BUF).get();
        int tailIndex = tailIndexOf(buf.slice(head.length, buf.readableBytes() - head.length));
        if (tailIndex == -1) {
            ctx.channel().attr(TEMP_BUF).set(buf);
            return false;
        }
        tailIndex += head.length;
        ctx.channel().attr(SEND_BUF).set(buf.slice(0, tailIndex + tail.length));
        ctx.channel().attr(TEMP_BUF).set(buf.slice(tailIndex + tail.length, buf.writerIndex() - tailIndex - tail.length).copy());
        return true;
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

    private HeadTailDecoder(Byte[] head, Byte[] tail, long maxTime, int maxLength) {
        super(head, maxTime, maxLength);
        this.tail = tail;
    }

    /**
     * 头尾解码器构造器
     *
     * @param head 头标识
     * @param tail 尾标识
     * @return {@link Builder} 构造器
     */
    public static Builder builder(Byte[] head, Byte[] tail) {
        return new Builder(head, tail);
    }

    /**
     * 头尾解码器构造器
     *
     * @param head 头标识
     * @param tail 尾标识
     * @return {@link Builder} 构造器
     */
    public static Builder builder(byte[] head, Byte[] tail) {
        return new Builder(toByteArray(head), tail);
    }

    /**
     * 头尾解码器构造器
     *
     * @param head 头标识
     * @param tail 尾标识
     * @return {@link Builder} 构造器
     */
    public static Builder builder(Byte[] head, byte[] tail) {
        return new Builder(head, toByteArray(tail));
    }

    /**
     * 头尾解码器构造器
     *
     * @param head 头标识
     * @param tail 尾标识
     * @return {@link Builder} 构造器
     */
    public static Builder builder(byte[] head, byte[] tail) {
        return new Builder(toByteArray(head), toByteArray(tail));
    }

    public static class Builder {
        private final Byte[] head;
        private final Byte[] tail;
        private long maxTime = DEFAULT_MAX_TIME;
        private int maxLength = DEFAULT_MAX_LENGTH;

        private Builder(Byte[] head, Byte[] tail) {
            this.head = head;
            this.tail = tail;
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

        public HeadTailDecoder build() {
            return new HeadTailDecoder(head, tail, maxTime, maxLength);
        }
    }
}
