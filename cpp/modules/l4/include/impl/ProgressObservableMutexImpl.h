#pragma once
#include "ProgressObserver.h"
#include <mutex>
#include <condition_variable>

template <typename T>
class ProgressObservableMutexImpl : public ProgressObservable<T>, public ProgressObserver<T>
{
protected:
    std::mutex mutex;
    std::condition_variable variable;
    bool done;
    T data;

public:
    ProgressObservableMutexImpl() : done(false) {}
    void setData(const T &newData, bool complete) override
    {
        std::unique_lock<std::mutex> lock(mutex);
        data = newData;
        done = complete;
        variable.notify_all();
    }
    T getData() override
    {
        std::unique_lock<std::mutex> lock(mutex);
        return data;
    }
    T wait() override
    {
        std::unique_lock<std::mutex> lock(mutex);
        variable.wait(lock);
        return data;
    }
    std::future<T> waitAsync() override
    {
        return std::async(std::launch::async, [this]() {
            return this->wait();
        });
    }
    bool isComplete() override
    {
        std::unique_lock<std::mutex> lock(mutex);
        return done;
    }
    void subscribe(
        std::function<void(const T &)> onData,
        std::function<void(const T &)> onComplete) override
    {
        std::unique_lock<std::mutex> lock(mutex);
        while (!done)
        {
            variable.wait(lock);
            if (!done)
                onData(data);
        }
        onComplete(data);
    }
};
