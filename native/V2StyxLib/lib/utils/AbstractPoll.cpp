/*
 * AbstractPoll.cpp
 *
 */

#include "utils/AbstractPoll.h"

template <class T>
AbstractPoll<T>::AbstractPoll() {
}

template <class T>
AbstractPoll<T>::~AbstractPoll() {
}

template <class T>
T AbstractPoll<T>::getFreeItem() {

}

template <class T>
bool AbstractPoll<T>::release(T id) {

}

template <class T>
void AbstractPoll<T>::clean() {
	mAvailable.clear();
	mLast = 0;
}
