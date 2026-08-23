#pragma once
#include <iostream>
#include <future>
#include <memory>
#include <vector>
#include "SerializerL4.h"

class TestDeserializerL4 : public styxlib::DeserializerL4
{
private:
    styxlib::ChannelTxPtr _channelTx;
    std::unique_ptr<std::promise<uint16_t>> receivedBytesPromise;
    uint32_t totalReceivedBytes{0};
    std::vector<uint8_t> lastReceivedBuffer;
public:
    TestDeserializerL4() {}
    virtual ~TestDeserializerL4() = default;
    void setChannelTx(styxlib::ChannelTxPtr channelTx) { _channelTx = channelTx; }
    styxlib::ErrorCode handleBuffer(
        styxlib::ClientId clientId,
        const styxlib::StyxBuffer buffer,
        styxlib::Size size) override
    {
        totalReceivedBytes += size;
        lastReceivedBuffer.assign(buffer, buffer + size);
        if (receivedBytesPromise) {
            receivedBytesPromise->set_value(size);
            receivedBytesPromise = nullptr;
        }
        std::string msg((const char*)buffer, size);
        std::cout << "Received from client " << clientId << ": " << msg << std::endl;
        const char* response = "Message received";
        _channelTx->sendBuffer(clientId, (const styxlib::StyxBuffer)response, strlen(response));
        return styxlib::ErrorCode::Success;
    }
    std::future<uint16_t> getReceivedBytes() { 
        receivedBytesPromise = std::make_unique<std::promise<uint16_t>>();
        return receivedBytesPromise->get_future();
    }
    uint32_t getTotalReceivedBytes() const { return totalReceivedBytes; }
    const std::vector<uint8_t> &getLastReceivedBuffer() const { return lastReceivedBuffer; }
};
