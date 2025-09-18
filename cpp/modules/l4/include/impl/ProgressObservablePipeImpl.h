#pragma once
#include "ProgressObserver.h"


template <typename T>
class ProgressObservablePipeImpl : public ProgressObservable<T>
{
public:
    static constexpr uint8_t TYPE_DATA = 1;
    static constexpr uint8_t TYPE_COMPLETE = 2;

protected:
    std::function<std::vector<char>(T)> serializer;
    int fd[2];
    bool ownFd {true};

public:
    ProgressObservablePipeImpl(
        std::function<std::vector<char>(T)> transformFunction) : serializer(transformFunction)
    {
        if (pipe(fd) == -1)
        {
            throw ProgressObserverException("Can't create pipe");
        }
    }
    ProgressObservablePipeImpl(
            std::function<std::vector<char>(T)> transformFunction, int writeFd) : serializer(transformFunction)
    {
        ownFd = false;
        fd[1] = writeFd;
        fd[0] = -1;
    }
    ~ProgressObservablePipeImpl()
    {
        if (ownFd) {
            close(fd[1]);
            close(fd[0]);
        }
    }
    void setData(const T &newData, bool complete) override
    {
        auto data = serializer(newData);
        uint32_t dataSize = htonl(data.size());
        write(fd[1], complete ? &TYPE_COMPLETE : &TYPE_DATA, 1);
        write(fd[1], &dataSize, 4);
        write(fd[1], data.data(), data.size());
        //flush(fd[1]);
    }
    int getReadFd() { return fd[0]; }
};

template <typename T>
class ProgressObserverPipeImpl : public ProgressObserver<T>
{
private:
    T data;
    std::function<T(char *, size_t)> deserializer;
    int readFd;
    bool done;

private:
    void readData()
    {
        uint32_t size;
        read(readFd, &size, 4);
        size = ntohl(size);
        if (size > 16 * 1024)
            throw ProgressObserverException("Size overflow");
        char *buffer = new char[size];
        ssize_t readed = read(readFd, buffer, size);
        data = deserializer(buffer, readed);
        delete [] buffer;
    }

public:
    ProgressObserverPipeImpl(int readFd,
                             std::function<T(char *, size_t)> deserializer) : readFd(readFd), deserializer(deserializer), done(false)
    {
    }
    T getData() override { return data; }
    T wait() override
    {
        uint8_t type;
        read(readFd, &type, 1);
        if (type == ProgressObservablePipeImpl<T>::TYPE_COMPLETE)
        {
            done = true;
            readData();
        }
        else if (type == ProgressObservablePipeImpl<T>::TYPE_DATA)
        {
            readData();
        }
        return data;
    }
    bool isComplete() override{ return done; }
    void subscribe(std::function<void(const T &)> callback, std::function<void(const T &)> complete) override
    {
        while (!done)
        {
            wait();
            if (!done)
                callback(data);
        }
        complete(data);
    }
};
