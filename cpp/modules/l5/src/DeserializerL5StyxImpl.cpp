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
        const uint8_t type = input.readUInt8();
        const uint32_t version = input.readUInt32();
        const uint64_t path = input.readUInt64();
        return {type, version, path};
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
        serialization::IBufferReader &input) const
    {
        const Type type = input.readUInt8();
        const Tag tag = input.readUInt16();

        switch (type)
        {
        case enums::Tversion: 
        {
            const uint32_t iounit = input.readUInt32();
            const std::string version = input.readUTFString();
            return messageFactory.constructTVersion(
                iounit,
                version);
        }
        case enums::Rversion:
        {
            const uint32_t maxPacketSize = input.readUInt32();
            const StyxString protocolVersion = input.readUTFString();
            return messageFactory.constructRVersion(
                tag,
                maxPacketSize,
                protocolVersion);
        }

        case enums::Tauth:
        {
            const Fid fid = input.readUInt32();
            const StyxString userName = input.readUTFString();
            const StyxString mountPoint = input.readUTFString();
            return messageFactory.constructTAuth(
                fid,
                userName,
                mountPoint);
        }

        case enums::Tflush:
        {
            const Tag oldTag = input.readUInt16();
            return messageFactory.constructTFlushMessage(oldTag);
        }

        case enums::Tattach:
        {
            const Fid fid = input.readUInt32();
            const Fid authFid = input.readUInt32();
            const StyxString userName = input.readUTFString();
            const StyxString mountPoint = input.readUTFString();
            return messageFactory.constructTAttach(
                fid,
                authFid,
                userName,
                mountPoint);
        }

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
        {
            const structs::QID qid = deserializeQid(input);
            return messageFactory.constructRAuthMessage(tag, qid);
        }

        case enums::Rerror:
        {
            const StyxString errorMessage = input.readUTFString();
            return messageFactory.constructRerror(tag, errorMessage);
        }

        case enums::Rflush:
            return messageFactory.constructRFlush(tag);

        case enums::Rattach:
        {
            const structs::QID qid = deserializeQid(input);
            return messageFactory.constructRAttachMessage(tag, qid);
        }

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
        {
            const Fid fid = input.readUInt32();
            const uint8_t mode = input.readUInt8();
            return messageFactory.constructTOpenMessage(
                fid,
                mode);
        }

        case enums::Ropen:
        {
            const structs::QID qid = deserializeQid(input);
            const uint32_t ioUnit = input.readUInt32();
            return messageFactory.constructROpenMessage(
                tag,
                qid,
                ioUnit);
        }

        case enums::Tcreate:
        {
            const Fid fid = input.readUInt32();
            const StyxString name = input.readUTFString();
            const uint32_t permissions = input.readUInt32();
            const uint8_t mode = input.readUInt8();
            return messageFactory.constructTCreateMessage(
                fid,
                name,
                permissions,
                mode);
        }

        case enums::Rcreate:
        {
            const structs::QID qid = deserializeQid(input);
            const uint32_t ioUnit = input.readUInt32();
            return messageFactory.constructRCreateMessage(
                tag,
                qid,
                ioUnit);
        }

        case enums::Tread:
        {
            const Fid fid = input.readUInt32();
            const uint64_t offset = input.readUInt64();
            const int count = static_cast<int>(input.readUInt32());
            return messageFactory.constructTReadMessage(
                fid,
                offset,
                count);
        }

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
        {
            const uint32_t count = input.readUInt32();
            return messageFactory.constructRWriteMessage(tag, count);
        }

        case enums::Tclunk:
        {
            const Fid fid = input.readUInt32();
            return messageFactory.constructTClunk(fid);
        }

        case enums::Rclunk:
        {
            const Fid fid = input.readUInt32();
            return messageFactory.constructRClunk(tag, fid);
        }

        case enums::Tremove:
        {
            const Fid fid = input.readUInt32();
            return messageFactory.constructTRemove(fid);
        }

        case enums::Rremove:
            return messageFactory.constructRRemove(tag);

        case enums::Tstat:
        {
            const Fid fid = input.readUInt32();
            return messageFactory.constructTStat(fid);
        }

        case enums::Rstat:
        {
            const uint16_t statSize = input.readUInt16();
            (void)statSize;
            const StyxStat stat = deserializeStat(input);
            return messageFactory.constructRStatMessage(tag, stat);
        }

        case enums::Twstat:
        {
            const Fid fid = input.readUInt32();
            const uint16_t statSize = input.readUInt16();
            (void)statSize;
            const StyxStat stat = deserializeStat(input);
            return messageFactory.constructTWStatMessage(fid, stat);
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
            auto message = deserializeMessage(reader);
            if (message.has_value())
            {
                messages::base::StyxMessageUPtr &msg = message.value();
                std::printf("DeserializerL5StyxImpl: Successfully deserialized message of type %d and tag %d for client %d\n", 
                    static_cast<int>(msg->getType()),
                    static_cast<int>(msg->getTag()), clientId);
                getConsumer()->handleMessage(clientId, msg);
            } else {
                std::cerr << "DeserializerL5StyxImpl: Failed to deserialize message for client " 
                    << clientId << ". Error code: " << static_cast<int>(message.error()) << std::endl;
                return message.error();
            }
            std::printf("DeserializerL5StyxImpl: Finished handling buffer for client %d\n", clientId);
            return ErrorCode::Success;
        }
        catch (const std::out_of_range &)
        {
            std::printf("DeserializerL5StyxImpl: Buffer too small while handling buffer for client %d\n", clientId);
            return ErrorCode::BufferTooSmall;
        }
        catch (const std::invalid_argument &)
        {
            std::printf("DeserializerL5StyxImpl: Invalid argument while handling buffer for client %d\n", clientId);
            return ErrorCode::NullptrArgument;
        }
        catch (...)
        {
            std::printf("DeserializerL5StyxImpl: Unexpected error while handling buffer for client %d\n", clientId);
            return ErrorCode::ConfigureFailed;
        }
    }
}
