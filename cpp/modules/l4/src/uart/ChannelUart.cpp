#include "uart/ChannelUart.h"

namespace styxlib
{
    
ChannelUartConfig::ChannelUartConfig(uint8_t config)
{
    this->config = config;
    this->useStreamingMode = (config & V2STYXLIB_CONFIG_STREAMING_MODE) != 0;
    this->sendCrc16 = (config & V2STYXLIB_CONFIG_SEND_CRC16) != 0;
    this->sofMarker1 = (config & V2STYXLIB_CONFIG_STREAMING_MODE) != 0 ? V2STYXLIB_SOF_MARKER_1 : 0;
    this->sofMarker2 = (config & V2STYXLIB_CONFIG_STREAMING_MODE) != 0 ? V2STYXLIB_SOF_MARKER_2 : 0;
    this->packetSizeHeader = PacketHeaderSize::Size2Bytes; // Default packet size header
}

}