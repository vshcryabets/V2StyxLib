#pragma once
#include <functional>


// #include <mutex>
// #include <condition_variable>
// #include <unistd.h>
// #include <arpa/inet.h>

template <typename T>
class ProgressObservable
{
public:
    virtual void setData(const T &newData, bool complete) = 0;
};

template <typename T>
class ProgressObserver
{
public:
    /**
     * Get last data set by observable
     */
    virtual T getData() = 0;
    /**
     * Wait for new data from observable
     */
    virtual T wait() = 0;
    virtual bool isComplete() = 0;
    /**
     * Subscribe to updates from the observable.
     * This method is synchronous and will block until the observable is complete.
     * onData will be called when new data is available.
     * onComplete will be called when the observable is complete.
     */
    virtual void subscribe(std::function<void(const T &)> onData,
                           std::function<void(const T &)> onComplete) = 0;
};

