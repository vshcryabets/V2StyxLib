package com.v2soft.styxlib.l4;

public enum ErrorCode {
    Success,
    AlreadyStarted,
    NotConnected,
    CantCreateSocket,
    CantBindSocket,
    CantListenSocket,
    CantCreateSocketPoll,
    PacketTooLarge,
    UnknownClient,
    BufferTooSmall,
    InvalidHeaderSize,
    NullptrArgument,
    SendFailed,
    ConfigureFailed,
}
