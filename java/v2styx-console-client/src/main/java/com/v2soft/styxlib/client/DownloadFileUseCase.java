package com.v2soft.styxlib.client;

import com.v2soft.styxlib.l5.Connection;

import java.io.File;
import java.util.function.Consumer;

public interface DownloadFileUseCase {
    record Progress(
            long totalSizeBytes,
            long processedBytes,
            long timeDeltaMs
    ) {
    }

    void download(Connection connection,
                  String srcFileName,
                  File dstFile,
                  Consumer<Progress> progress,
                  Consumer<Throwable> error);
}
