package com.v2soft.styxlib.client;

import com.v2soft.styxlib.l5.Connection;

import java.io.File;
import java.util.function.Consumer;

public interface UploadFileUseCase {
    record Progress(
            long totalSizeBytes,
            long processedBytes,
            long timeDeltaMs
    ) {
    }

    void upload(Connection connection,
                  File srcFileName,
                  String dstFile,
                  Consumer<Progress> progress,
                  Consumer<Throwable> error);
}
