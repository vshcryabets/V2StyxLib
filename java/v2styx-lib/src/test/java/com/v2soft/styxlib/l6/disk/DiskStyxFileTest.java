package com.v2soft.styxlib.l6.disk;

import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l6.vfs.DiskStyxFile;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.EmptyChannelDriver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class DiskStyxFileTest {
    @Test
    void testFileReading() throws IOException {
        var fileName = UUID.randomUUID().toString();
        File testFile = new File(fileName);
        testFile.createNewFile();
        var outputStream = new FileOutputStream(testFile);
        for (int i = 0; i< 512; i++)
            outputStream.write(i);
        outputStream.close();
        var styxFile = new DiskStyxFile(testFile);
        ClientDetails client = new ClientDetails(new EmptyChannelDriver(), 1);
        styxFile.open(client, ModeType.OREAD);
        var buffer = new byte[8192];
        int read = styxFile.read(client, buffer, 0, 8192);
        Assertions.assertEquals(512, read);
        read = styxFile.read(client, buffer, 512, 8192);
        Assertions.assertEquals(0, read);
        styxFile.close(client);
    }
}
