package com.github.zzxt0019.netty.decoder;

import com.github.zzxt0019.netty.transfer.IntTransfer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * 头长尾解码器
 * <p>
 * 相对于使用[(头长+尾判断) 或者 (头尾+长度判断)]的方法
 * <p>
 * 头长尾解码可以在判断失败后, 将data内部的头长解析出来
 */
public class HeadLengthTailDecoder extends HeadDecoder {
    /**
     * 数据长度
     */
    private static final AttributeKey<Integer> DATA_LENGTH = AttributeKey.valueOf("com.github.zzxt0019.netty.DataLength-" + attribute_key_count);
    /**
     * 长度解析器
     */
    private final IntTransfer lengthTransfer;
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
        if (ctx.channel().attr(DATA_LENGTH).get() == null) {
            if (buf.resetReaderIndex().readableBytes() >= head.length + lengthTransfer.getLength()) {
                ctx.channel().attr(DATA_LENGTH).set(lengthTransfer.getFunction().apply(buf.slice(head.length, lengthTransfer.getLength())));
            } else {
                return false;
            }
        }
        if (buf.resetReaderIndex().readableBytes() >= head.length + lengthTransfer.getLength() + ctx.channel().attr(DATA_LENGTH).get() + tail.length) {
            ByteBuf thisBuf = buf.slice(0, head.length + lengthTransfer.getLength() + ctx.channel().attr(DATA_LENGTH).get() + tail.length);
            if (tailIndexOf(thisBuf.slice(head.length + lengthTransfer.getLength() + ctx.channel().attr(DATA_LENGTH).get(), tail.length)) == -1) {
                ctx.channel().attr(SEND_BUF).set(null);
                ctx.channel().attr(TEMP_BUF).set(buf.slice(1, buf.resetReaderIndex().readableBytes() - 1));
            } else {
                ctx.channel().attr(SEND_BUF).set(thisBuf);
                ctx.channel().attr(TEMP_BUF).set(buf.slice(head.length + lengthTransfer.getLength() + ctx.channel().attr(DATA_LENGTH).get() + tail.length, buf.resetReaderIndex().readableBytes() - head.length - lengthTransfer.getLength() - ctx.channel().attr(DATA_LENGTH).get() - tail.length).copy());
            }
            ctx.channel().attr(DATA_LENGTH).set(null);
            return true;
        }
        return false;
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

    private HeadLengthTailDecoder(Byte[] head, IntTransfer lengthTransfer, Byte[] tail, long maxTime, int maxLength) {
        super(head, maxTime, maxLength);
        this.lengthTransfer = lengthTransfer;
        this.tail = tail;
    }

    /**
     * 头长尾解码器构造器
     *
     * @param head           头标识
     * @param lengthTransfer 长度解码器
     * @param tail           尾标识
     * @return {@link Builder} 构造器
     */
    public static Builder builder(Byte[] head, IntTransfer lengthTransfer, Byte[] tail) {
        return new Builder(head, lengthTransfer, tail);
    }

    /**
     * 头长尾解码器构造器
     *
     * @param head           头标识
     * @param lengthTransfer 长度解码器
     * @param tail           尾标识
     * @return {@link Builder} 构造器
     */
    public static Builder builder(byte[] head, IntTransfer lengthTransfer, Byte[] tail) {
        return new Builder(toByteArray(head), lengthTransfer, tail);
    }

    /**
     * 头长尾解码器构造器
     *
     * @param head           头标识
     * @param lengthTransfer 长度解码器
     * @param tail           尾标识
     * @return {@link Builder} 构造器
     */
    public static Builder builder(Byte[] head, IntTransfer lengthTransfer, byte[] tail) {
        return new Builder(head, lengthTransfer, toByteArray(tail));
    }

    /**
     * 头长尾解码器构造器
     *
     * @param head           头标识
     * @param lengthTransfer 长度解码器
     * @param tail           尾标识
     * @return {@link Builder} 构造器
     */
    public static Builder builder(byte[] head, IntTransfer lengthTransfer, byte[] tail) {
        return new Builder(toByteArray(head), lengthTransfer, toByteArray(tail));
    }

    public static class Builder {
        private final Byte[] head;
        private final IntTransfer lengthTransfer;
        private final Byte[] tail;
        private long maxTime = DEFAULT_MAX_TIME;
        private int maxLength = DEFAULT_MAX_LENGTH;

        private Builder(Byte[] head, IntTransfer lengthTransfer, Byte[] tail) {
            this.head = head;
            this.lengthTransfer = lengthTransfer;
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

        public HeadLengthTailDecoder build() {
            return new HeadLengthTailDecoder(head, lengthTransfer, tail, maxTime, maxLength);
        }
    }
}
