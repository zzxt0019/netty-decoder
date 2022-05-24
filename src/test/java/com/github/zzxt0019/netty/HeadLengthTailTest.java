package com.github.zzxt0019.netty;

import com.github.zzxt0019.netty.decoder.HeadLengthTailDecoder;
import com.github.zzxt0019.netty.transfer.IntTransfer;

public class HeadLengthTailTest {
    public static class Server {
        public static void main(String[] args) {
            TestServer.server(() ->
                    HeadLengthTailDecoder.builder(
                            "aa".getBytes(),
                            IntTransfer.buildStr(2),
                            "bb".getBytes()
                    ).build()
            );
        }
    }

    public static class Client {
        public static void main(String[] args) {
            TestClient.sendStr(() -> new String[]{
                    "zzaa01xbbzz",//aa01xbb
                    "zzaa02czbczzqq",
                    "wwzzaa10zzzzzaa01xbbzz",//aa05aa01xbb
                    "zzaa06aa01xbbzz",//aa01xbb 不能及时出, 要等后一个扫描完06的内容
                    "zzaa01qbbzz",//aa01qbb
                    "aa01qbbaa02wwbb",// aa01qbb aa02wwbb
                    "a",
                    "a",
                    "0",
                    "1",
                    "i",
                    "b",
                    "b",//aa01ibb
            });
        }
    }
}
