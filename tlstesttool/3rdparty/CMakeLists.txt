cmake_minimum_required(VERSION 3.4.0)

include(ExternalProject)

set (EP_ASIO "EP_ASIO")
set (EP_ZLIB "EP_ZLIB")
set (EP_MBEDTLS "EP_MBEDTLS")
set (EP_OPENSSL "EP_OPENSSL")

set (SSL_CONF_PLATFORM "linux-x86_64")
set (EXTRA_CMAKE_ARGS)

ExternalProject_Add(${EP_ASIO}
	URL http://downloads.sourceforge.net/project/asio/asio/1.12.2%20%28Stable%29/asio-1.12.2.tar.gz
	URL_HASH SHA256=da5c9fd2726165dc85a4fd3cb86ccd2059ffaa332675aa861262f5b1923ce30e
	BUILD_ALWAYS ON
    CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR> --build=${ARCH_TRIPLET} --host=${ARCH_TRIPLET} --without-boost --without-openssl CC=${CMAKE_C_COMPILER} CXX=${CMAKE_CXX_COMPILER} CPPFLAGS=${ASIO_FLAGS}
    CONFIGURE_COMMAND ""
    BUILD_COMMAND ""
    INSTALL_COMMAND ${CMAKE_COMMAND} -E copy_directory
                    <SOURCE_DIR>/include ${EP_INSTALL_DIR}/include/

)
list (APPEND EXTRA_CMAKE_ARGS
	-DAsio_INCLUDE_DIR=${EP_INSTALL_DIR}/include
)

list (APPEND EXTRA_CMAKE_ARGS
	-DEP_INCLUDEDIR=${EP_INSTALL_DIR}/include
	-DEP_LIBDIR=${EP_INSTALL_DIR}/lib   
)

ExternalProject_Add(${EP_ZLIB}
	URL http://download.sourceforge.net/project/libpng/zlib/1.2.11/zlib-1.2.11.tar.gz
	URL_HASH SHA256=c3e5e9fdd5004dcb542feda5ee4f0ff0744628baf8ed2dd5d66f8ca1197cb1a1
	PATCH_COMMAND patch -p0 < ${CMAKE_CURRENT_LIST_DIR}/zlib-RenameLibraryOutputName.patch && patch -p0 < ${CMAKE_CURRENT_LIST_DIR}/zlib-DoNotInstallDynamicLibrary.patch
	BUILD_ALWAYS ON
	INSTALL_DIR ${EP_INSTALL_DIR}
	CMAKE_ARGS -DCMAKE_INSTALL_PREFIX:PATH=<INSTALL_DIR> -DCMAKE_BUILD_TYPE:STRING=${CMAKE_BUILD_TYPE}
)
  
ExternalProject_Add(${EP_MBEDTLS}
	DEPENDS ${EP_ZLIB}
	URL https://github.com/Mbed-TLS/mbedtls/archive/refs/tags/mbedtls-2.2.1.tar.gz
	URL_HASH SHA256=32819c62c20e8740a11b49daa5d09ac6f179edf120a87ac559cd63120b66b699
	PATCH_COMMAND patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-EnableZlibSupport.patch && patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-OverwriteClientHelloExtensions.patch &&  patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-OverwriteClientHelloCompressionMethods.patch &&  patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-LongHexDump.patch &&  patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-LogServerHelloExtensions.patch &&  patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-AllowAllCipherSuites.patch &&  patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-OverwriteHelloVersion.patch &&  patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-LogServerHelloCipherSuite.patch &&  patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-ExtensionsOutput.patch && patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-OverwriteServerHelloCompressionMethods.patch && patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-OverwriteEllipticCurveGroup.patch  && patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-OverwriteServerHelloExtensions.patch && patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/mbedTLS-ForceCertificateUsage.patch
	
	BUILD_ALWAYS ON
	INSTALL_DIR ${EP_INSTALL_DIR}
	CMAKE_ARGS -DCMAKE_INSTALL_PREFIX:PATH=<INSTALL_DIR> -DCMAKE_BUILD_TYPE:STRING=${CMAKE_BUILD_TYPE} -DENABLE_ZLIB_SUPPORT:BOOL=ON -DENABLE_TESTING:BOOL=OFF -DENABLE_PROGRAMS:BOOL=OFF -DZLIB_LIBRARY=<INSTALL_DIR>/lib -DZLIB_INCLUDE_DIR=<INSTALL_DIR>/include
	TEST_BEFORE_INSTALL 0
)

ExternalProject_Add(${EP_OPENSSL}
	URL https://www.openssl.org/source/openssl-3.0.1.tar.gz
	URL_HASH SHA256=c311ad853353bce796edad01a862c50a8a587f62e7e2100ef465ab53ec9b06d1
	PATCH_COMMAND patch -p1 < ${CMAKE_CURRENT_LIST_DIR}/OpenSSL-OverwriteExtensions.patch
	BUILD_ALWAYS ON
	BUILD_IN_SOURCE 1
	INSTALL_DIR ${EP_INSTALL_DIR}
	CONFIGURE_COMMAND <SOURCE_DIR>/Configure --prefix=<INSTALL_DIR> no-shared no-dtls enable-ec_nistp_64_gcc_128 enable-weak-ssl-ciphers ${SSL_CONF_PLATFORM}
	BUILD_COMMAND make depend && make
)
list (APPEND EXTRA_CMAKE_ARGS
	-DOPENSSL_ROOT_DIR=${EP_INSTALL_DIR}
)

set (DEPENDENCIES)
list (APPEND DEPENDENCIES ${EP_ASIO} ${EP_ZLIB} ${EP_MBEDTLS} ${EP_OPENSSL})

ExternalProject_Add (EP_TLSTESTTOOL
	DEPENDS ${DEPENDENCIES}
	SOURCE_DIR ${PROJECT_SOURCE_DIR}
	CMAKE_ARGS -DUSE_3RDPARTYBUILD=OFF ${EXTRA_CMAKE_ARGS}
	BUILD_ALWAYS ON
	INSTALL_COMMAND ""
	BINARY_DIR ${CMAKE_CURRENT_BINARY_DIR}
)
