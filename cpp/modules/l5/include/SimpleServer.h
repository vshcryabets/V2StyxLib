#pragma once

#include <atomic>
#include <future>
#include <thread>

#include "dataL5.h"
#include "ChannelDriver.h"

namespace styxlib
{
    class SimpleServer: public DeserializerL5::Consumer
    {
    public:
        struct Configuration
        {
            ChannelDriverPtr channel;
            DeserializerL5Ptr deserializer;
            SerializerL5Ptr serializer;
        };
    private:
        DeserializerL5Ptr deserializer;
        ChannelDriverPtr channel;
        std::atomic<bool> running{false};
    public:
        SimpleServer(const SimpleServer::Configuration &config);
        ~SimpleServer();
        std::future<ErrorCode> start();
        std::future<void> stop();
        bool isStarted() const;
        virtual void handleMessage(
            ClientId clientId, 
            const styxlib::messages::base::StyxMessageUPtr &message
        ) override;
    };
}