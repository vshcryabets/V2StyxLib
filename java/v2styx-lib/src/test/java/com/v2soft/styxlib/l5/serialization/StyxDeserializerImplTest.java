package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.io.impl.BufferImpl;
import com.v2soft.styxlib.l5.messages.StyxROpenMessage;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.impl.BufferReaderImpl;
import com.v2soft.styxlib.l5.serialization.impl.StyxDeserializerImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class StyxDeserializerImplTest {

    @Test
    public void testDeserializationStyxStat() throws StyxException {
        var dataBuffer = new byte[]{71, 0, //0
                0x22, 0x11, //type //2
                (byte) 0xE7, (byte) 0x89, 0x0E, 0x00,// 4:dev
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

    @Test
    public void testDeserializeTStat() throws StyxException {
        var dataBuffer = new byte[]{11, 0, 0, 0,
                (byte) MessageType.Tstat.getByte(),
                (byte) 0x11, (byte) 0xFF, // tag
                0x01, 0x02, 0x03, 0x04 // FID
        };
        var buffer = new BufferImpl(dataBuffer.length);
        buffer.write(dataBuffer, 0, dataBuffer.length);
        var bufferReader = new BufferReaderImpl(buffer);
        var deserializer = new StyxDeserializerImpl();
        var message = deserializer.deserializeMessage(bufferReader, 8192);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0xFF11, message.getTag());
        Assertions.assertEquals(StyxTMessageFID.class, message.getClass());
        Assertions.assertEquals(0x04030201, ((StyxTMessageFID) message).getFID());
        Assertions.assertEquals(0, buffer.remainsToRead());
    }

    @Test
    public void testDeserializeRCreate() throws StyxException {
        var dataBuffer = new byte[]{
                0x18, 0x00, 0x00, 0x00,
                0x73, // Rcreate
                0x01, 0x00, //Tag
                0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xd2, 0x70, 0x79, 0x19, 0x00, 0x00, 0x00, 0x00, // QID
                0x00, 0x20, 0x00, 0x00 // iounit

        };
        var buffer = new BufferImpl(dataBuffer.length);
        buffer.write(dataBuffer, 0, dataBuffer.length);
        var bufferReader = new BufferReaderImpl(buffer);
        var deserializer = new StyxDeserializerImpl();
        var message = (StyxROpenMessage) deserializer.deserializeMessage(bufferReader, 8192);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(MessageType.Rcreate, message.getType());
        Assertions.assertEquals(1, message.getTag());
        var qid = message.getQID();
        Assertions.assertEquals(0, qid.getType().getByte());
        Assertions.assertEquals(0, qid.getVersion());
        Assertions.assertEquals(427389138, qid.getPath());
        Assertions.assertEquals(8192, message.ioUnit);
    }
}
