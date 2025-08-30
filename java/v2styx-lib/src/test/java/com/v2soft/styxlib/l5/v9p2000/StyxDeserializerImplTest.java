package com.v2soft.styxlib.l5.v9p2000;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.io.impl.BufferImpl;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;
import com.v2soft.styxlib.l5.messages.base.MessagesFactory;
import com.v2soft.styxlib.l5.messages.v9p2000.MessageFactoryImpl;
import com.v2soft.styxlib.l5.serialization.impl.BufferReaderImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StyxDeserializerImplTest {
    MessagesFactory messageFactory = new MessageFactoryImpl();
    StyxDeserializerImpl deserializer = new StyxDeserializerImpl(messageFactory);

    @Test
    public void testDeserializationStyxStat() throws StyxException {
        var dataBuffer = new byte[]{71, 0, //0
                0x22, 0x11, //type //2
                (byte) 0xE7, (byte) 0x89, 0x0E, 0x00,// 4:dev
                QidType.QTFILE, // 8: QTFILE(0x00)
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
        var stat = deserializer.deserializeStat(bufferReader);
        Assertions.assertNotNull(stat);
    }

    @Test
    public void testDeserializeTStat() throws StyxException {
        var dataBuffer = new byte[]{11, 0, 0, 0,
                (byte) MessageType.Tstat,
                (byte) 0x11, (byte) 0xFF, // tag
                0x01, 0x02, 0x03, 0x04 // FID
        };
        var buffer = new BufferImpl(dataBuffer.length);
        buffer.write(dataBuffer, 0, dataBuffer.length);
        var bufferReader = new BufferReaderImpl(buffer);
        var message = deserializer.deserializeMessage(bufferReader, 8192);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0xFF11, message.getTag());
        Assertions.assertEquals(BaseMessage.class, message.getClass());
        Assertions.assertEquals(0x04030201, ((BaseMessage) message).getFID());
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
        var message = (BaseMessage) deserializer.deserializeMessage(bufferReader, 8192);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(MessageType.Rcreate, message.type);
        Assertions.assertEquals(1, message.getTag());
        var qid = message.getQID();
        Assertions.assertEquals(0, qid.type());
        Assertions.assertEquals(0, qid.version());
        Assertions.assertEquals(427389138, qid.path());
        Assertions.assertEquals(8192, message.getIounit());
    }

    @Test
    public void testDeserializeQid() throws StyxException {
        byte[] dataBuffer = {
                (byte) QidType.QTDIR,
                (byte) 0xF1, 0x70, 0x74, 0x6A, //9: qid.version[4] 0x6A7470F1
                0x04, 0x51, (byte) 0x9E, 0x04, 0x51, (byte) 0x9E, 0x30, 0x12 //13: qid.path[8] 0x12309E51049E5104L
        };
        var buffer = new BufferImpl(dataBuffer.length);
        buffer.write(dataBuffer, 0, dataBuffer.length);
        var bufferReader = new BufferReaderImpl(buffer);

        var qid = deserializer.deserializeQid(bufferReader);
        Assertions.assertEquals(QidType.QTDIR, qid.type());
        Assertions.assertEquals(0x6A7470F1, qid.version());
        Assertions.assertEquals(0x12309E51049E5104L, qid.path());
    }

    @Test
    public void testDeserializeTRemove() throws StyxException {
        var dataBuffer = new byte[]{11, 0, 0, 0,
                (byte) MessageType.Tremove,
                (byte) 0x11, (byte) 0xFF, // tag
                0x01, 0x02, 0x03, 0x04 // FID
        };
        var buffer = new BufferImpl(dataBuffer.length);
        buffer.write(dataBuffer, 0, dataBuffer.length);
        var bufferReader = new BufferReaderImpl(buffer);
        var message = deserializer.deserializeMessage(bufferReader, 8192);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0xFF11, message.getTag());
        Assertions.assertEquals(BaseMessage.class, message.getClass());
        Assertions.assertEquals(0x04030201, ((BaseMessage) message).getFID());
        Assertions.assertEquals(0, buffer.remainsToRead());
    }
}
