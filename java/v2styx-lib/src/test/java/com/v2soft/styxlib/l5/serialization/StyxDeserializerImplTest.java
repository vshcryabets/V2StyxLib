package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.l5.io.impl.BufferImpl;
import com.v2soft.styxlib.l5.serialization.impl.BufferReaderImpl;
import com.v2soft.styxlib.l5.serialization.impl.StyxDeserializerImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class StyxDeserializerImplTest {

    @Test
    public void testDeserializationStyxStat() throws IOException {
        var dataBuffer = new byte[]{71, 0, //0
                0x22, 0x11, //type //2
                (byte) 0xE7,  (byte) 0x89, 0x0E, 0x00,// 4:dev
                0x00, // 8: QTFILE(0x00)
                (byte) 0xF1, 0x70, 0x74, 0x6A, //9: qid.version[4] 0x6A7470F1
                0x04, 0x51, (byte) 0x9E, 0x04, 0x51, (byte) 0x9E, 0x30, 0x12, //13: qid.path[8] 0x12309E51049E5104L
                0x00, 0x01, 0x00, 0x00, //21: mode[4]
                0x40, 0x30, 0x20, 0x70, // 25:atime[4]
                0x40, 0x30, 0x20, 0x70, // 29: mtime[4]
                0x23, 0x01, (byte) 0xE7, 0x0F, 0x00, 0x00, 0x00, 0x00, //33:length[8]
                0x08, 0x00, //41: filename size
                'f', 'i', 'l', 'e', 'n', 'a', 'm', 'e', //43: name[ s ]
                0x05, 0x00, // owner name size
                'o', 'w', 'n', 'e', 'r',
                0x05, 0x00, //group name size
                'g', 'r', 'o', 'u', 'p',
                0x06, 0x00, // editor name size
                'e', 'd', 'i', 't', 'o', 'r'
        };
        var buffer = new BufferImpl(dataBuffer.length);
        buffer.write(dataBuffer, 0, dataBuffer.length);
        var bufferReader = new BufferReaderImpl(buffer);
        var deserializer = new StyxDeserializerImpl();
        var stat = deserializer.deserializeStat(bufferReader);
        Assertions.assertNotNull(stat);

    }
}
