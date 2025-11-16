#include "ChannelUnixPipeImpl.h"

namespace styxlib
{
    ChannelUnixPipeImpl::ChannelUnixPipeImpl(const PacketHeaderSize header)
        : ChannelUnixFile(header)
    {
        int pipeFds[2];
        if (pipe(pipeFds) == -1)
        {
            throw std::runtime_error("Failed to create pipe");
        }
        readFd = pipeFds[0];
        writeFd = pipeFds[1];
    }
} // namespace styxlib