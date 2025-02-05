package com.v2soft.styxlib.client;

import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

public class DownloadFileUseCaseImpl implements DownloadFileUseCase {

    @Override
    public void download(Connection connection, String srcFileName, File dstFile, Consumer<Progress> progress, Consumer<Throwable> error) {
        try {
            var src = connection.open(srcFileName);
            if (!src.exists()) {
                error.accept(new FileNotFoundException(String.format("File %s doesn't exists", srcFileName)));
                return;
            }
            var stat = src.getStat();
            var totalLength = stat.length();
            progress.accept(new Progress(totalLength, 0, 0));
            var inputStream = src.openForReadUnbuffered();
            var outputStream = new FileOutputStream(dstFile); //new File(localFolder, fileName));
            var bufferSize = inputStream.ioUnit() - IDataSerializer.BASE_BINARY_SIZE - 4;
            var buffer = new byte[bufferSize];
            var read = 0;
            long totalRead = 0;
            long lastMark = totalRead;
            long startTime = System.currentTimeMillis();
            do {
                read = inputStream.read(buffer);
                if (read > 0) {
                    outputStream.write(buffer, 0, read);
                    totalRead += read;
                }
                if (totalRead - lastMark > 256000) {
                    long timeDelta = System.currentTimeMillis() - startTime;
                    progress.accept(new Progress(totalLength, totalRead, timeDelta));
                    lastMark = totalRead;
                }
            } while (read > 0);
            long timeDelta = System.currentTimeMillis() - startTime;
            progress.accept(new Progress(totalLength, totalRead, timeDelta));
            inputStream.close();
            outputStream.close();
            src.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
