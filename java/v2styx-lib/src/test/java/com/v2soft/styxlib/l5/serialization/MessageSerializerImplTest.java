package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.QIDType;
import com.v2soft.styxlib.l5.messages.StyxTVersionMessage;
import com.v2soft.styxlib.l5.serialization.impl.BufferWritterImpl;
import com.v2soft.styxlib.l5.serialization.impl.MessageSerializerImpl;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

public class MessageSerializerImplTest {

    @Test
    public void testSerializationTMessages() throws IOException {
        var serializer = new MessageSerializerImpl();
        var buffer = ByteBuffer.allocate(8192);
        var outputBuffer = new BufferWritterImpl(buffer);
        serializer.serialize(new StyxTVersionMessage(128, "9P2000"), outputBuffer);
        byte[] data = new byte[buffer.limit()];
        buffer.position(0);
        buffer.get(data);
        Assertions.assertArrayEquals(new byte[]{19, 0, 0, 0,
                (byte) MessageType.Tversion.getByte(),
                (byte) 0xFF, (byte) 0xFF, // tag
                (byte) 0x80, 0, 0, 0,  // io unit
                // proto string size
                0x06, 0x00,
                // proto string
                0x39, 'P', 0x32, 0x30, 0x30, 0x30}, data, "TVersion");
    }

    @Test
    public void testSerializationStyxStat() throws IOException {
        var serializer = new MessageSerializerImpl();
        var buffer = ByteBuffer.allocate(8192);
        var outputBuffer = new BufferWritterImpl(buffer);
        var fileName = "filename";
        var accessTime = new Date(0x70203040L * 1000);
        var stat = new StyxStat(
                (short) 0x1122,
                0x000E89E7,
                new StyxQID(QIDType.QTFILE,
                        0x6A7470F1,
                        0x12309E51049E5104L),
                (int) FileMode.ReadOwnerPermission.getMode(),
                accessTime,
                accessTime,
                0x0FE70123L,
                fileName,
                "owner",
                "group",
                "editor"
        );
        serializer.serializeStat(stat, outputBuffer);
        byte[] data = new byte[buffer.position()];
        buffer.position(0);
        buffer.get(data);
        Assertions.assertArrayEquals(new byte[]{71, 0, //0
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
                },
                data,
                "StyxSTat serialization failed");
    }
}
