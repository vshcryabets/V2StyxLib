#pragma once
#include <cstdint>

namespace styxlib::dev {
    class Metrics {
        private:
            size_t byteBufferAllocation = 0;
            size_t byteArrayAllocation = 0;
            size_t newStyxMessage = 0;
            size_t byteArrayAllocationRRead = 0;
            size_t byteArrayAllocationTWrite = 0;
            size_t byteArrayAllocationIo = 0;
        public:
            static Metrics& getInstance() {
                static Metrics instance;
                return instance;
            }

            void incrementStyxMessage();
    };

}