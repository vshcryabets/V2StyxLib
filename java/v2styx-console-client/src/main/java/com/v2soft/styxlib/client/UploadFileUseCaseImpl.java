package com.v2soft.styxlib.client;

import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;

public class UploadFileUseCaseImpl implements UploadFileUseCase {
    @Override
    public void upload(Connection connection,
                       File srcFile,
                       String dstFileName,
                       Consumer<Progress> progress,
                       Consumer<Throwable> errorHandler) {
        try {
            var dst = connection.open(dstFileName);
            dst.create(FileMode.WriteOwnerPermission.getMode() | FileMode.ReadOwnerPermission.getMode());
            if (!dst.exists()) {
                errorHandler.accept(new FileNotFoundException(String.format("File %s doesn't exists", dstFileName)));
                return;
            }
            var totalLength = srcFile.length();
            progress.accept(new UploadFileUseCase.Progress(totalLength, 0, 0));

            var inputStream = new FileInputStream(srcFile);
            var outputStream = dst.openForWriteUnbuffered();
            var bufferSize = outputStream.ioUnit() - IDataSerializer.BASE_BINARY_SIZE - 4;
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
                    progress.accept(new UploadFileUseCase.Progress(totalLength, totalRead, timeDelta));
                    lastMark = totalRead;
                }
            } while (read > 0);
            long timeDelta = System.currentTimeMillis() - startTime;
            progress.accept(new UploadFileUseCase.Progress(totalLength, totalRead, timeDelta));
            // close everything
            outputStream.close();
            inputStream.close();
            dst.close();

        } catch (IOException e) {
            errorHandler.accept(e);
        }

    }
}
