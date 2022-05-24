package com.github.zzxt0019.netty;

import com.github.zzxt0019.netty.decoder.HeadLengthDecoder;
import com.github.zzxt0019.netty.transfer.IntTransfer;

public class HeadLengthTest {
    public static class Server {
        public static void main(String[] args) throws InterruptedException {
            TestServer.server(() -> HeadLengthDecoder.builder(new Byte[]{'z',null,null,'a'}, IntTransfer.buildStr(2)).build());
        }
    }
    public static class Client {
        public static void main(String[] args) {
            TestClient.sendStr(()-> new String[]{
                    "zzaa01x", //aa01x
                    "zzaa01zx", //aa01z
                    "zzaa02asd", //aa02as
                    "zzaa03xxccvvzb", //aa03xxc
                    "a",
                    "a",
                    "0",
                    "4",
                    "123",
                    "456"//aa041234
            });
        }
    }
}
