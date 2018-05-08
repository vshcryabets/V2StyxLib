/*
 * Main.cpp
 *
 *  Created on: May 07, 2018
 *      Author: vshcryabets@gmail.com
 */
#include "gtest/gtest.h"
#include "stdio.h"

int main(int argc, char **argv) {
	printf("Start tests\n");
	::testing::InitGoogleTest(&argc, argv);
  	return RUN_ALL_TESTS();
}
