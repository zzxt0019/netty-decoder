package com.github.zzxt0019.netty.decoder;

import com.github.zzxt0019.netty.transfer.IntTransfer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * 头长解码器
 */
public class HeadLengthDecoder extends HeadDecoder {
    /**
     * 数据长度
     */
    private static final AttributeKey<Integer> DATA_LENGTH = AttributeKey.valueOf("com.github.zzxt0019.netty.DataLength-" + attribute_key_count);
    /**
     * 长度解析器
     */
    private final IntTransfer lengthTransfer;

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
        if (buf.resetReaderIndex().readableBytes() >= head.length + lengthTransfer.getLength() + ctx.channel().attr(DATA_LENGTH).get()) {
            ctx.channel().attr(SEND_BUF).set(buf.slice(0, head.length + lengthTransfer.getLength() + ctx.channel().attr(DATA_LENGTH).get()));
            ctx.channel().attr(TEMP_BUF).set(buf.slice(head.length + lengthTransfer.getLength() + ctx.channel().attr(DATA_LENGTH).get(), buf.resetReaderIndex().readableBytes() - head.length - lengthTransfer.getLength() - ctx.channel().attr(DATA_LENGTH).get()).copy());
            ctx.channel().attr(DATA_LENGTH).set(null);
            return true;
        }
        return false;
    }

    private HeadLengthDecoder(Byte[] head, IntTransfer lengthTransfer, long maxTime, int maxLength) {
        super(head, maxTime, maxLength);
        this.lengthTransfer = lengthTransfer;
    }

    /**
     * 头长解码器构造器
     *
     * @param head           头标识
     * @param lengthTransfer 长度解码器
     * @return {@link Builder} 构造器
     */
    public static Builder builder(Byte[] head, IntTransfer lengthTransfer) {
        return new Builder(head, lengthTransfer);
    }

    /**
     * 头长解码器构造器
     *
     * @param head           头标识
     * @param lengthTransfer 长度解码器
     * @return {@link Builder} 构造器
     */
    public static Builder builder(byte[] head, IntTransfer lengthTransfer) {
        return new Builder(toByteArray(head), lengthTransfer);
    }

    public static class Builder {
        private final Byte[] head;
        private final IntTransfer lengthTransfer;
        private long maxTime = DEFAULT_MAX_TIME;
        private int maxLength = DEFAULT_MAX_LENGTH;

        private Builder(Byte[] head, IntTransfer lengthTransfer) {
            this.head = head;
            this.lengthTransfer = lengthTransfer;
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

        public HeadLengthDecoder build() {
            return new HeadLengthDecoder(head, lengthTransfer, maxTime, maxLength);
        }
    }
}
