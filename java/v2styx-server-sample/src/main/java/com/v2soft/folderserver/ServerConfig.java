package com.v2soft.folderserver;

import java.util.List;

public record ServerConfig(
        short port,
        List<String> interfaces,
        String exportPath
) {
}
