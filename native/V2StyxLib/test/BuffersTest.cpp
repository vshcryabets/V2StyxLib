#include <algorithm>

#include "BufferTest.h"
#include "gtest/gtest.h"
#include "io/StyxByteBufferReadable.h"

TEST(cpp_sorter_test, int_arr_sort) {
	std::srand(std::time(0));
	int maxSize = 8192;
	StyxByteBufferReadable *readableBuffer = new StyxByteBufferReadable(maxSize);
	uint8_t *testBuffer = new uint8_t[maxSize];
	uint8_t *readBuffer = new uint8_t[maxSize];

	for (size_t i = 0; i < maxSize; i++ ) {
		testBuffer[i] = std::rand();
	}

	for (size_t i = 1; i < maxSize; i++) {
		readableBuffer->write(testBuffer, 0, i);
		int read = readableBuffer.read(readBuffer, 0, i);
		assertEquals("Wrong read bytes count", i, read);
		// check read bytes
		for (int j = 0; j < i; j++) {
			if (readBuffer[j] != testBuffer[j]) {
				assertTrue("Wrong byte at position " + j, false);
			}
		}
	}
}
