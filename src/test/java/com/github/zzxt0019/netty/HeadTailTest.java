package com.github.zzxt0019.netty;

import com.github.zzxt0019.netty.decoder.HeadTailDecoder;

public class HeadTailTest {
    public static class Server{
        public static void main(String[] args) {
            TestServer.server(() -> HeadTailDecoder.builder("aa".getBytes(), "bb".getBytes()).build());
        }
    }
    public static class Client{
        public static void main(String[] args) {
            TestClient.sendStr(() -> new String[]{
                    "aabb",//aabb
                    "aa-text1-bb",//aa-text1-bb
                    "ddaa-text2-bbzz",//aa-text2-bb
                    "dda",
                    "a-t",
                    "ext",
                    "3-b",
                    "bzz",//aa-text3-bb
                    "aaabbaaxbbaazbb"
            });
        }
    }
}
