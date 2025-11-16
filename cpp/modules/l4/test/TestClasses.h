#pragma once
#include <iostream>
#include <future>
#include <memory>
#include "SerializerL4.h"

class TestDeserializerL4OneToOne : public styxlib::DeserializerL4
{
private:
    std::weak_ptr<styxlib::ChannelTx> _channelTx;
    std::unique_ptr<std::promise<uint16_t>> receivedBytesPromise;
    uint32_t totalReceivedBytes{0};
public:
    TestDeserializerL4OneToOne() {}
    virtual ~TestDeserializerL4OneToOne() = default;
    void setChannelTx(styxlib::ChannelTxPtr channelTx) { _channelTx = channelTx; }
    void handleBuffer(
        styxlib::ClientId clientId,
        const styxlib::StyxBuffer buffer,
        styxlib::Size size) override
    {
        totalReceivedBytes += size;
        if (receivedBytesPromise) {
            receivedBytesPromise->set_value(size);
            receivedBytesPromise = nullptr;
        }
        std::string msg((const char*)buffer, size);
        std::cout << "Received from client " << clientId << ": " << msg << std::endl;
        if (auto p = _channelTx.lock()) {
            const char* response = "Message received";
            p->sendBuffer((const styxlib::StyxBuffer)response, strlen(response));
        }
    }
    std::future<uint16_t> getReceivedBytes() { 
        receivedBytesPromise = std::make_unique<std::promise<uint16_t>>();
        return receivedBytesPromise->get_future();
    }
    uint32_t getTotalReceivedBytes() const { return totalReceivedBytes; }
};

class TestDeserializerL4OneToMany : public styxlib::DeserializerL4
{
private:
    std::weak_ptr<styxlib::ChannelTxOneToMany> _channelTx;
    std::unique_ptr<std::promise<uint16_t>> receivedBytesPromise;
    uint32_t totalReceivedBytes{0};
public:
    TestDeserializerL4OneToMany() {}
    virtual ~TestDeserializerL4OneToMany() = default;
    void setChannelTx(styxlib::ChannelTxOneToManyPtr channelTx) { _channelTx = channelTx; }
    void handleBuffer(
        styxlib::ClientId clientId,
        const styxlib::StyxBuffer buffer,
        styxlib::Size size) override
    {
        totalReceivedBytes += size;
        if (receivedBytesPromise) {
            receivedBytesPromise->set_value(size);
            receivedBytesPromise = nullptr;
        }
        std::string msg((const char*)buffer, size);
        std::cout << "Received from client " << clientId << ": " << msg << std::endl;
        if (auto p = _channelTx.lock()) {
            const char* response = "Message received";
            p->sendBuffer(clientId, (const styxlib::StyxBuffer)response, strlen(response));
        }
    }
    std::future<uint16_t> getReceivedBytes() { 
        receivedBytesPromise = std::make_unique<std::promise<uint16_t>>();
        return receivedBytesPromise->get_future();
    }
    uint32_t getTotalReceivedBytes() const { return totalReceivedBytes; }
};