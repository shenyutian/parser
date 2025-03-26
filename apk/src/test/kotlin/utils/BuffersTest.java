package utils;

import org.junit.jupiter.api.Test;
import org.syt.parser.apk.utils.Buffers;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class BuffersTest {

    @Test
    public void testGetUnsignedByte() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[]{2, -10});
        assertEquals(2, Buffers.readUByte(byteBuffer));
        assertEquals(246, Buffers.readUByte(byteBuffer));
    }
}