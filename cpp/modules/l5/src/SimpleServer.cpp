#include "SimpleServer.h"

#include <iostream>

#include "enums/MessageType.h"
#include "messages/v9p2000/BaseMessage.h"

namespace styxlib
{
    SimpleServer::SimpleServer(
        const SimpleServer::Configuration &config
    )
        : iounit(config.iounit),
          deserializer(config.deserializer),
          channel(config.channel),
          serializer(config.serializer),
          messageFactory(config.messageFactory)
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
        const styxlib::StyxMessageUPtr &message
    ) 
    {
        switch (message->getType())
        {
            case styxlib::enums::Tversion: {
                auto tversionMessage = dynamic_cast<const styxlib::messages::v9p2000::BaseMessage*>(message.get());
                // TODO close all files
                // TODO stop IO operations
                // TODO reset state
                uint16_t iounit = (this->iounit < tversionMessage->getIounit()) ? this->iounit : tversionMessage->getIounit();
                auto answer = messageFactory->constructRVersion(
                    message->getTag(),
                    iounit,
                    "9P2000");                
                serializer->sendMessage(clientId, answer);
                break;
            }

            default:
                std::cout << "Received message from client " << clientId 
                  << " with type " << static_cast<int>(message->getType()) 
                  << " and tag " << static_cast<int>(message->getTag()) 
                  << std::endl;
                break;
        }

    }
}