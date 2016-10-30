#include <algorithm>
#include <ctime>

#include "BufferTest.h"
#include "gtest/gtest.h"
#include "io/StyxByteBufferReadable.h"

TEST(cpp_sorter_test, int_arr_sort) {
	std::srand(std::time(0));
	size_t maxSize = 8192;
	StyxByteBufferReadable *readableBuffer = new StyxByteBufferReadable(maxSize);
	uint8_t *testBuffer = new uint8_t[maxSize];
	uint8_t *readBuffer = new uint8_t[maxSize];

	for (size_t i = 0; i < maxSize; i++ ) {
		testBuffer[i] = std::rand();
	}

	for (size_t i = 1; i < maxSize; i++) {
		readableBuffer->write(testBuffer, 0, i);
		size_t read = readableBuffer->read(readBuffer, 0, i);
		ASSERT_EQ(i, read) << "Wrong read bytes count";
		// check read bytes
		for (size_t j = 0; j < i; j++) {
			if (readBuffer[j] != testBuffer[j]) {
				assertTrue("Wrong byte at position " + j, false);
			}
		}
	}
}
