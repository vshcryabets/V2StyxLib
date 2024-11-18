package com.v2soft.styxserver.domain;

public record ServerConfiguration(
        int port,
        String rottDirectory
) {
}
