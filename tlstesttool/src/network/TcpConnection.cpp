/*
* TLS-Test Tool
* The TLS Test Tool checks the TLS configuration and compliance with the protocol specification for TLS servers and clients.
*
* Licensed under EUPL-1.2-or-later.
*
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at the LICENSE.md file or visit
*
* https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/
#include "TcpConnection.h"
#include "asio.hpp"

namespace TlsTestTool {

class TcpConnection::Data {
public:
	asio::ip::tcp::socket socket;
	bool connectionClosedByError;
	std::vector<std::reference_wrapper<AbstractSocketObserver>> observers;

   Data(asio::io_context* io_context) : socket(*io_context), connectionClosedByError(false), observers()  {
   }

   void closeConnectionByError() {
	   connectionClosedByError = true;
   }

   void registerObserver(AbstractSocketObserver & observer) {
	   observers.emplace_back(std::ref(observer));
   }

   void notifyWrite(std::size_t length) {
	   for (auto & observer : observers) {
		   observer.get().onBlockWritten(length);
	   }
   }

   void notifyRead(std::size_t length) {
	   for (auto & observer : observers) {
		   observer.get().onBlockRead(length);
	   }
   }
};

TcpConnection::TcpConnection(asio::io_context* io_context) : impl(std::make_unique<Data>(io_context)) {
}

TcpConnection::~TcpConnection() = default;

void TcpConnection::close() {
	impl->socket.shutdown(asio::ip::tcp::socket::shutdown_both);
	impl->socket.close();
}

std::size_t TcpConnection::write(const std::vector<char> & data) {
	try {
		const auto numBytesWritten = asio::write(impl->socket, asio::buffer(data));
		impl->notifyWrite(numBytesWritten);
		return numBytesWritten;
	} catch (const asio::system_error & e) {
		if ((asio::error::connection_aborted == e.code()) || (asio::error::connection_reset == e.code())) {
			impl->closeConnectionByError();
		}
		throw e;
	}
}

std::vector<char> TcpConnection::read(std::size_t length) {
	try {
		std::vector<char> buffer(length);
		asio::read(impl->socket, asio::buffer(buffer));
		impl->notifyRead(buffer.size());
		return buffer;
	} catch (const asio::system_error & e) {
		if ((asio::error::connection_aborted == e.code()) || (asio::error::connection_reset == e.code())) {
			impl->closeConnectionByError();
		}
		throw e;
	}
}

std::size_t TcpConnection::available() const {
	return impl->socket.available();
}

bool TcpConnection::isClosed() {
	if (impl->connectionClosedByError) {
		return true;
	}
	if (!impl->socket.is_open()) {
		return true;
	}
	{
		// No connection, if the remote endpoint cannot be accessed.
		asio::error_code ec;
		impl->socket.remote_endpoint(ec);
		if (ec) {
			return true;
		}
	}
	// Check, if the socket is marked readable, which it is, when it is closed (similar to select).
	bool isReadable = false;
	bool isAborted = false;
	bool isReset = false;
	impl->socket.async_read_some(asio::null_buffers(), [&](const asio::error_code & error, std::size_t) {
		if (!error) {
			isReadable = true;
		}
		if (asio::error::connection_aborted == error) {
			isAborted = true;
		}
		if (asio::error::connection_reset == error) {
			isReset = true;
		}
	});

	impl->socket.get_executor().context().restart();
	impl->socket.get_executor().context().poll();
	
	impl->socket.cancel();
	// Check, if the number of readable bytes is zero (similar to ioctl with FIONREAD).
	const bool nothingToRead = (0 == available());
	return (isReadable && nothingToRead) || isAborted || isReset;
}

std::string TcpConnection::getRemoteIpAddress() const {
	return impl->socket.remote_endpoint().address().to_string();
}

uint16_t TcpConnection::getRemoteTcpPort() const {
	return impl->socket.remote_endpoint().port();
}

void TcpConnection::registerObserver(AbstractSocketObserver & observer) {
	impl->registerObserver(observer);
}
int TcpConnection::getSocketFileDesriptor() {
	return impl->socket.native_handle();
}

asio::ip::tcp::socket & TcpConnection::getSocket() {
	return impl->socket;
}

}

