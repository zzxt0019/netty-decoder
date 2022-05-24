package com.github.zzxt0019.netty.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 头解码器
 */
public class HeadDecoder extends CachedDecoder {
    /**
     * 当前实例是当前类
     */
    private final boolean CURRENT_INSTANCE;
    /**
     * 头标识
     */
    protected final Byte[] head;

    /**
     * 判断是否有头标志, 并截取TEMP_BUF头标志及之后内容并发送SEND_BUF
     *
     * @param ctx ctx
     * @return 是否通过
     * @throws Exception ex
     */
    @Override
    protected boolean handle(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = ctx.channel().attr(TEMP_BUF).get();
        int headIndex = headIndexOf(buf);
        if (headIndex != -1) {
            buf = buf.slice(headIndex, buf.resetReaderIndex().readableBytes() - headIndex);
            if (CURRENT_INSTANCE) {
                ctx.channel().attr(SEND_BUF).set(buf);
            }
        }
        ctx.channel().attr(TEMP_BUF).set(CURRENT_INSTANCE && headIndex == -1 ? null : buf);
        return headIndex != -1;
    }

    /**
     * 查询头标志位置
     *
     * @param buf 整体buf
     * @return 头index
     */
    protected int headIndexOf(ByteBuf buf) {
        return indexOf(buf, head);
    }

    private int containsWithNull(Byte[] str, byte b) {
        for (int i = str.length - 1; i >= 0; i--) {
            if (str[i] == null || str[i] == b) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 查询标志位置
     *
     * @param buf    整体buf
     * @param target 标志
     * @return index
     */
    protected int indexOf(ByteBuf buf, Byte[] target) {
        if (buf == null) {
            return -1;
        }
        buf = buf.duplicate();
        int bufLength = buf.readableBytes();
        int readerIndex = buf.readerIndex();
        int targetLength = target.length;
        int i = 0, j = 0;
        while (i <= bufLength - targetLength + j) {
            if (target[j] != null && buf.getByte(i + readerIndex) != target[j]) {
                if (i == bufLength - targetLength + j) {
                    break;
                }
                int pos = containsWithNull(target, buf.getByte(i + readerIndex + targetLength - j));
                if (pos == -1) {
                    i = i + targetLength + 1 - j;
                } else {
                    i = i + targetLength - pos - j;
                }
                j = 0;
            } else {
                if (j == targetLength - 1) {
                    return i - j;
                } else {
                    i++;
                    j++;
                }
            }
        }
        return -1;
    }

    /**
     * 查询标志位置
     *
     * @param buf    整体buf
     * @param target 标志
     * @return index
     */
    @Deprecated
    protected int indexOf2(ByteBuf buf, Byte[] target) {
        if (buf == null) {
            return -1;
        }
        buf = buf.duplicate();
        int bufIndex = 0, targetIndex = 0;
        while (buf.isReadable()) {
            bufIndex++;
            byte read = buf.readByte();
            if (target[targetIndex] == null || target[targetIndex] == read) {
                if (++targetIndex == target.length) {
                    return bufIndex - target.length;
                }
            } else {
                buf = buf.readerIndex(bufIndex -= targetIndex);
                targetIndex = 0;
            }
        }
        return -1;
    }

    protected HeadDecoder(Byte[] head, long maxTime, int maxLength) {
        super(maxTime, maxLength);
        this.head = head;
        this.CURRENT_INSTANCE = getClass() == HeadDecoder.class;
    }

    protected static Byte[] toByteArray(byte[] bytes) {
        Byte[] h = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            h[i] = bytes[i];
        }
        return h;
    }

    /**
     * 头解码器构造器
     *
     * @param head 头标识
     * @return {@link Builder} 构造器
     */
    public static Builder builder(Byte[] head) {
        return new Builder(head);
    }

    /**
     * 头解码器构造器
     *
     * @param head 头标识
     * @return {@link Builder} 构造器
     */
    public static Builder builder(byte[] head) {
        return new Builder(toByteArray(head));
    }

    public static class Builder {
        private final Byte[] head;
        private long maxTime = DEFAULT_MAX_TIME;
        private int maxLength = DEFAULT_MAX_LENGTH;

        private Builder(Byte[] head) {
            this.head = head;
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

        public HeadDecoder build() {
            return new HeadDecoder(head, maxTime, maxLength);
        }
    }
}
