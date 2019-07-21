#include <algorithm>
#include <ctime>

#include "gtest/gtest.h"
#include "io/StyxByteBufferReadable.h"

TEST(cpp_byte_buffer_readable_test, rw_test) {
	std::srand(std::time(0));
	size_t maxSize = 8192;
	StyxByteBufferReadable readableBuffer(maxSize);
	uint8_t testBuffer[maxSize];
	uint8_t readBuffer[maxSize];

	for (size_t i = 0; i < maxSize; i++ ) {
		testBuffer[i] = std::rand();
	}

	for (size_t i = 1; i < maxSize; i++) {
		readableBuffer.write(testBuffer, i);
		ASSERT_EQ(i, readableBuffer.remainsToRead()) << "Wrong buffer size";
		size_t read = readableBuffer.read(readBuffer, i);
		ASSERT_EQ(i, read) << "Wrong read bytes count";
		// check read bytes
		for (size_t j = 0; j < i; j++) {
			if (readBuffer[j] != testBuffer[j]) {
				ASSERT_TRUE(false) << "Wrong byte at position " << j;
			}
		}
	}
}
