#include "SimpleServer.h"

#include <iostream>

namespace styxlib
{
    SimpleServer::SimpleServer(
        const SimpleServer::Configuration &config
    )
        : deserializer(config.deserializer),
          channel(config.channel)
    {
    }

    SimpleServer::~SimpleServer() = default;

    std::future<ErrorCode> SimpleServer::start()
    {
        return std::async(
            std::launch::async,
            [this]()
            {
                if (running.load())
                {
                    return ErrorCode::AlreadyStarted;
                }
                running.store(true);
                deserializer->setConsumer(this);
                channel->setDeserializer(deserializer);
                auto startResult = channel->start().get();
                if (startResult != ErrorCode::Success)
                {
                    running.store(false);
                    return startResult;
                }
                return ErrorCode::Success;
            });
    }

    std::future<void> SimpleServer::stop()
    {
        return std::async(
            std::launch::async,
            [this]()
            {
                if (!running.load())
                {
                    return;
                }
                channel->stop().get();
                deserializer->setConsumer(nullptr);
                running.store(false);
            });
    }

    bool SimpleServer::isStarted() const
    {
        return running.load();
    }

    void SimpleServer::handleMessage(
        ClientId clientId, 
        const styxlib::messages::base::StyxMessageUPtr &message
    ) 
    {
        std::cout << "Received message from client " << clientId 
                  << " with type " << static_cast<int>(message->getType()) 
                  << " and tag " << static_cast<int>(message->getTag()) 
                  << std::endl;
    }
}