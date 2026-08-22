#pragma once
#include "messages/base/StyxMessage.h"
#include "SerializerL4.h"

namespace styxlib
{
    class DeserializerL5: public DeserializerL4
    {
    public:
        class Consumer {
        public:
            virtual ~Consumer() = default;
            virtual void handleMessage(
                ClientId clientId, 
                const styxlib::messages::base::StyxMessageUPtr &message
            ) = 0;
        };
    protected:
        Consumer *consumer = nullptr;
        Consumer *getConsumer() const { return consumer; }
    public:
        DeserializerL5() : DeserializerL4(), consumer(nullptr) {}
        virtual ~DeserializerL5() = default;
        void setConsumer(Consumer *consumer) { this->consumer = consumer; }
    };
}