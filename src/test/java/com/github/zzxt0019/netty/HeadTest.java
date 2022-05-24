package com.github.zzxt0019.netty;

import com.github.zzxt0019.netty.decoder.HeadDecoder;

public class HeadTest {
    public static class Server {
        public static void main(String[] args) {
            TestServer.server(() -> HeadDecoder.builder(new Byte[]{
                    null,'a','a'
            }).build());
        }
    }

    public static class Client {
        public static void main(String[] args) {
            TestClient.sendStr(() -> new String[]{
                    "za",
                    "aq",
                    "aaabb",
                    "sdfsdaa-text1-bb",
                    "ddaaa-text2-bbzsdfsdz",
                    "asdfafaaa",
                    "bbbsdfsdf",//只有头, 上一行已解析这一行是废弃的
                    "sssaaasdfasdf",
                    "m",
                    "aaxxx"
            });
        }
    }
}
