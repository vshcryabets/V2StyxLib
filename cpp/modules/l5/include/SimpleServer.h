#pragma once

#include <atomic>
#include <future>
#include <thread>

#include "dataL5.h"
#include "serialization/DeserializerL5.h"
#include "serialization/SerializerL5.h"
#include "ChannelDriver.h"
#include "messages/v9p2000/MessageFactoryImpl.h"

namespace styxlib
{
    class SimpleServer: public styxlib::serialization::DeserializerL5::Consumer
    {
    public:
        struct Configuration
        {
            uint16_t iounit{8192};
            ChannelDriverPtr channel;
            DeserializerL5Ptr deserializer;
            SerializerL5Ptr serializer;
            MessageFactoryPtr messageFactory;
        };
    private:
        uint16_t iounit;
        DeserializerL5Ptr deserializer;
        SerializerL5Ptr serializer;
        ChannelDriverPtr channel;
        MessageFactoryPtr messageFactory;
        std::atomic<bool> running{false};
    public:
        SimpleServer(const SimpleServer::Configuration &config);
        ~SimpleServer();
        std::future<ErrorCode> start();
        std::future<void> stop();
        bool isStarted() const;
        virtual void handleMessage(
            ClientId clientId, 
            const styxlib::StyxMessageUPtr &message
        ) override;
    };
}