package com.github.zzxt0019.netty.transfer;

import io.netty.buffer.ByteBuf;

import java.util.function.Function;

public abstract class Transfer<T> {
    /**
     * 解析长度
     */
    protected final int length;
    /**
     * 解析方法
     */
    protected final Function<ByteBuf, T> function;

    public Transfer(int length, Function<ByteBuf, T> function) {
        this.length = length;
        this.function = function;
    }

    public int getLength() {
        return length;
    }

    public Function<ByteBuf, T> getFunction() {
        return function;
    }
}
