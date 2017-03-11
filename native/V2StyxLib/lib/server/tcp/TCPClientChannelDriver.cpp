/*
 * TCPClientChannelDriver.cpp
 *
 */

#include "server/tcp/TCPClientChannelDriver.h"

TCPClientChannelDriver::TCPClientChannelDriver(StyxString address, uint16_t port, bool ssl) :
	TCPChannelDriver(address, port, ssl) {
	// TODO Auto-generated constructor stub

}

TCPClientChannelDriver::~TCPClientChannelDriver() {
	// TODO Auto-generated destructor stub
}

StyxThread TCPClientChannelDriver::start(int iounit) {
	return 0;
}

void TCPClientChannelDriver::prepareSocket(std::string socketAddress, bool ssl) throw() {

}

bool TCPClientChannelDriver::isConnected() {
	return false;
}

bool TCPClientChannelDriver::isStarted() {
	return false;
}

bool TCPClientChannelDriver::sendMessage(StyxMessage message, ClientDetails *recipient) throw() {
	return false;
}

void TCPClientChannelDriver::run() {

}

void TCPClientChannelDriver::close() throw() {

}

std::vector<ClientDetails*> TCPClientChannelDriver::getClients() {
	std::vector<ClientDetails*> result;
	return result;
}
