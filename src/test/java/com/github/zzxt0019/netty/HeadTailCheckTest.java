package com.github.zzxt0019.netty;

import com.github.zzxt0019.netty.decoder.HeadTailCheckDecoder;
import com.github.zzxt0019.netty.transfer.BoolTransfer;
import com.github.zzxt0019.netty.transfer.IntTransfer;

public class HeadTailCheckTest {
    public static class Server {
        public static void main(String[] args) throws InterruptedException {
//            TestServer.server(() -> HeadTailCheckDecoder.builder(new Byte[]{'b', 'b'}, new Byte[]{'b', 'b'},
//                    BoolTransfer.build(-2, buf -> buf.getByte(buf.readableBytes() - 1) == 'w')).build());
            TestServer.server(() -> HeadTailCheckDecoder.builder(new Byte[]{'u', 'u'}, new Byte[]{'u', 'u'},
                    BoolTransfer.build(0, buf -> buf.readableBytes() == IntTransfer.buildStr(2).getFunction().apply(buf.slice(buf.readableBytes() - 4, 2)) + 2 + 4)).build());
        }
    }

    public static class Client {
        public static void main(String[] args) {
//            TestClient.sendStr(() -> new String[]{
//                    "bbxxqqwwbbb",
//                    "bxxaabbzz",
//                    "qqwbb",
//                    "bbwbb"
//            });

            TestClient.sendStr(() -> new String[]{
                    "uu12303uu",
                    "zzuu",
                    "11301uu08",
                    "uu02uu15uu"
            });


        }
    }
}
