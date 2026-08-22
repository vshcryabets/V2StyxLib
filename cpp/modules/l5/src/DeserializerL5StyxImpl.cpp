#include "serialization/DeserializerL5StyxImpl.h"

#include <stdexcept>
#include <vector>
#include <iostream>

#include "enums/MessageType.h"
#include "serialization/BufferReaderImpl.h"

namespace styxlib
{
    structs::QID DeserializerL5StyxImpl::deserializeQid(serialization::IBufferReader &input) const
    {
        return {
            input.readUInt8(),
            input.readUInt32(),
            input.readUInt64()};
    }

    StyxStat DeserializerL5StyxImpl::deserializeStat(serialization::IBufferReader &input) const
    {
        input.readUInt16(); // Stat total size without this field.

        StyxStat result = StyxStat::EMPTY;
        result.type = static_cast<int>(input.readUInt16());
        result.dev = static_cast<long>(input.readUInt32());
        result.QID = deserializeQid(input);
        result.mode = static_cast<long>(input.readUInt32());
        result.accessTime = input.readUInt32();
        result.modificationTime = input.readUInt32();
        result.length = static_cast<long>(input.readUInt64());
        result.name = input.readUTFString();
        result.userName = input.readUTFString();
        result.groupName = input.readUTFString();
        result.modificationUser = input.readUTFString();
        return result;
    }

    StyxMessageExpected 
    DeserializerL5StyxImpl::deserializeMessage(
        serialization::IBufferReader &input,
        Size packetLimit) const
    {
        const Size packetSize = input.readUInt32();
        if (packetSize > packetLimit)
        {
            return Unexpected(ErrorCode::PacketTooLarge);
        }

        const Type type = input.readUInt8();
        const Tag tag = input.readUInt16();

        switch (type)
        {
        case enums::Tversion:
            return messageFactory.constructTVersion(
                input.readUInt32(),
                input.readUTFString());

        case enums::Rversion:
            return messageFactory.constructRVersion(
                tag,
                input.readUInt32(),
                input.readUTFString());

        case enums::Tauth:
            return messageFactory.constructTAuth(
                input.readUInt32(),
                input.readUTFString(),
                input.readUTFString());

        case enums::Tflush:
            return messageFactory.constructTFlushMessage(input.readUInt16());

        case enums::Tattach:
            return messageFactory.constructTAttach(
                input.readUInt32(),
                input.readUInt32(),
                input.readUTFString(),
                input.readUTFString());

        case enums::Twalk:
        {
            const Fid fid = input.readUInt32();
            const Fid newFid = input.readUInt32();
            const uint16_t count = input.readUInt16();
            std::vector<StyxString> path;
            path.reserve(count);
            for (uint16_t i = 0; i < count; ++i)
            {
                path.push_back(input.readUTFString());
            }
            return messageFactory.constructTWalkMessage(fid, newFid, path);
        }

        case enums::Rauth:
            return messageFactory.constructRAuthMessage(tag, deserializeQid(input));

        case enums::Rerror:
            return messageFactory.constructRerror(tag, input.readUTFString());

        case enums::Rflush:
            return messageFactory.constructRFlush(tag);

        case enums::Rattach:
            return messageFactory.constructRAttachMessage(tag, deserializeQid(input));

        case enums::Rwalk:
        {
            const uint16_t count = input.readUInt16();
            std::vector<structs::QID> qids;
            qids.reserve(count);
            for (uint16_t i = 0; i < count; ++i)
            {
                qids.push_back(deserializeQid(input));
            }
            return messageFactory.constructRWalkMessage(tag, qids);
        }

        case enums::Topen:
            return messageFactory.constructTOpenMessage(
                input.readUInt32(),
                input.readUInt8());

        case enums::Ropen:
            return messageFactory.constructROpenMessage(
                tag,
                deserializeQid(input),
                input.readUInt32());

        case enums::Tcreate:
            return messageFactory.constructTCreateMessage(
                input.readUInt32(),
                input.readUTFString(),
                input.readUInt32(),
                input.readUInt8());

        case enums::Rcreate:
            return messageFactory.constructRCreateMessage(
                tag,
                deserializeQid(input),
                input.readUInt32());

        case enums::Tread:
            return messageFactory.constructTReadMessage(
                input.readUInt32(),
                input.readUInt64(),
                static_cast<int>(input.readUInt32()));

        case enums::Rread:
        {
            const uint32_t dataLength = input.readUInt32();
            std::vector<uint8_t> data(dataLength);
            input.read(data.data(), dataLength);
            return messageFactory.constructRReadMessage(
                tag,
                data.data(),
                static_cast<int>(dataLength));
        }

        case enums::Twrite:
        {
            const Fid fid = input.readUInt32();
            const uint64_t offset = input.readUInt64();
            const uint32_t dataLength = input.readUInt32();
            std::vector<uint8_t> data(dataLength);
            input.read(data.data(), dataLength);
            return messageFactory.constructTWriteMessage(
                fid,
                offset,
                data.data(),
                0,
                static_cast<int>(dataLength));
        }

        case enums::Rwrite:
            return messageFactory.constructRWriteMessage(tag, input.readUInt32());

        case enums::Tclunk:
            return messageFactory.constructTClunk(input.readUInt32());

        case enums::Rclunk:
            return messageFactory.constructRClunk(tag, input.readUInt32());

        case enums::Tremove:
            return messageFactory.constructTRemove(input.readUInt32());

        case enums::Rremove:
            return messageFactory.constructRRemove(tag);

        case enums::Tstat:
            return messageFactory.constructTStat(input.readUInt32());

        case enums::Rstat:
        {
            input.readUInt16();
            return messageFactory.constructRStatMessage(tag, deserializeStat(input));
        }

        case enums::Twstat:
        {
            const Fid fid = input.readUInt32();
            input.readUInt16();
            return messageFactory.constructTWStatMessage(fid, deserializeStat(input));
        }

        case enums::Rwstat:
            return messageFactory.constructRWStat(tag);

        default:
            return Unexpected(ErrorCode::UnsupportedMessageType);
        }
    }

    ErrorCode DeserializerL5StyxImpl::handleBuffer(
        ClientId clientId,
        const StyxBuffer buffer,
        Size size)
    {
        if (buffer == nullptr)
        {
            return ErrorCode::NullptrArgument;
        }

        if (getConsumer() == nullptr)
        {
            return ErrorCode::Success;
        }

        std::cout << "DeserializerL5StyxImpl: Handling buffer of size " << size << " for client " << clientId << std::endl;

        try
        {
            BufferReaderImpl reader(buffer, size);
            auto message = deserializeMessage(reader, ioUnit);
            if (message.has_value())
            {
                getConsumer()->handleMessage(clientId, *message);
            } else {
                return message.error();
            }
            return ErrorCode::Success;
        }
        catch (const std::out_of_range &)
        {
            return ErrorCode::BufferTooSmall;
        }
        catch (const std::invalid_argument &)
        {
            return ErrorCode::NullptrArgument;
        }
        catch (...)
        {
            return ErrorCode::ConfigureFailed;
        }
    }
}
