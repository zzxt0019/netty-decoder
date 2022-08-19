package com.github.zzxt0019.netty.transfer;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * int转换器
 * <p>
 * 将{@link ByteBuf}转为{@link Integer}
 */
public class IntTransfer extends Transfer<Integer> {

    private IntTransfer(int length, Function<ByteBuf, Integer> lengthFunction) {
        super(length, lengthFunction);
    }

    /**
     * 创建新对象, 转换结果加num
     * <p>
     * 不修改原始对象
     *
     * @param num 数
     * @return 新的IntTransfer
     */
    public IntTransfer plus(int num) {
        return new IntTransfer(length, buf -> function.apply(buf) + num);
    }

    /**
     * 创建新对象, 转换结果减num
     * <p>
     * 不修改原始对象
     *
     * @param num 数
     * @return 新的IntTransfer
     */
    public IntTransfer minus(int num) {
        return new IntTransfer(length, buf -> function.apply(buf) - num);
    }


    /**
     * 创建新对象, 放弃前waitLength个字节再转换
     *
     * @param waitLength 前面的字节数
     * @return 新的IntTransfer
     */
    public IntTransfer wait(int waitLength) {
        return new IntTransfer(length + waitLength, buf -> function.apply(buf.slice(waitLength, length)));
    }

    /**
     * 普通地构建转换器
     *
     * @param length   解析长度
     * @param function 解析方法
     * @return IntTransfer
     */
    public static IntTransfer build(int length, Function<ByteBuf, Integer> function) {
        return new IntTransfer(length, function);
    }

    /**
     * 8位(1Byte)长度的转换器
     *
     * @param function 解析方法
     * @return IntTransfer
     */
    public static IntTransfer build8(Function<ByteBuf, Integer> function) {
        return new IntTransfer(1, function);
    }

    /**
     * 默认的8位(1Byte)长度的转换器
     *
     * @return IntTransfer
     */
    public static IntTransfer buildDefault8() {
        return new IntTransfer(1, buf -> (int) buf.readByte());
    }

    /**
     * 16位(2Byte)长度的转换器
     *
     * @param function 解析方法
     * @return IntTransfer
     */
    public static IntTransfer build16(Function<ByteBuf, Integer> function) {
        return new IntTransfer(2, function);
    }

    /**
     * 默认的16位(2Byte)长度的转换器
     *
     * @return IntTransfer
     */
    public static IntTransfer buildDefault16() {
        return new IntTransfer(2, buf -> (int) buf.readShort());
    }

    /**
     * 32位(4Byte)长度的转换器
     *
     * @param function 解析方法
     * @return IntTransfer
     */
    public static IntTransfer build32(Function<ByteBuf, Integer> function) {
        return new IntTransfer(4, function);
    }

    /**
     * 默认的32位(4Byte)长度的转换器
     *
     * @return IntTransfer
     */
    public static IntTransfer buildDefault32() {
        return new IntTransfer(4, ByteBuf::readInt);
    }

    /**
     * 64位(8Byte)长度的转换器
     *
     * @param function 解析方法
     * @return IntTransfer
     */
    public static IntTransfer build64(Function<ByteBuf, Integer> function) {
        return new IntTransfer(8, function);
    }

    /**
     * 默认的64位(8Byte)长度的转换器
     *
     * @return IntTransfer
     */
    public static IntTransfer buildDefault64() {
        return new IntTransfer(4, buf -> (int) buf.readLong());
    }

    /**
     * 字符串转换器
     *
     * @param length 解析长度
     * @return IntTransfer
     */
    public static IntTransfer buildStr(int length) {
        return new IntTransfer(length, buf -> Integer.parseInt(buf.toString(0, length, StandardCharsets.US_ASCII)));
    }
}
