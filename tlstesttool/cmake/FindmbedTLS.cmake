
find_path(mbedTLS_INCLUDE_DIR
    mbedtls/ssl.h
    HINTS ${EP_INCLUDEDIR}
)
find_library(mbedTLS_LIBRARY
    mbedtls
    HINTS ${EP_LIBDIR}
)
find_library(mbedTLS_CRYPTO_LIBRARY
    mbedcrypto
    HINTS ${EP_LIBDIR}
)
find_library(mbedTLS_X509_LIBRARY
    mbedx509
    HINTS ${EP_LIBDIR}
)


if(mbedTLS_INCLUDE_DIR AND mbedTLS_LIBRARY AND mbedTLS_CRYPTO_LIBRARY AND mbedTLS_X509_LIBRARY)
	set(mbedTLS_INCLUDE_DIRS ${mbedTLS_INCLUDE_DIR} ${ZLIB_INCLUDE_DIR})
	set(mbedTLS_LIBRARIES ${mbedTLS_LIBRARY} ${mbedTLS_X509_LIBRARY} ${mbedTLS_CRYPTO_LIBRARY} ${ZLIB_LIBRARY})

	if(NOT TARGET mbedTLS::mbedTLS)
		add_library(mbedTLS::mbedTLS UNKNOWN IMPORTED)
		set_target_properties(mbedTLS::mbedTLS PROPERTIES
			INTERFACE_INCLUDE_DIRECTORIES "${mbedTLS_INCLUDE_DIRS}"
			INTERFACE_LINK_LIBRARIES ZLIB::ZLIB)
		if(EXISTS "${mbedTLS_LIBRARY}")
			set_target_properties(mbedTLS::mbedTLS PROPERTIES
				IMPORTED_LOCATION "${mbedTLS_LIBRARY}")
		endif()
	endif()

	if(NOT TARGET mbedTLS::Crypto)
		add_library(mbedTLS::Crypto UNKNOWN IMPORTED)
		set_target_properties(mbedTLS::Crypto PROPERTIES
			INTERFACE_INCLUDE_DIRECTORIES "${mbedTLS_INCLUDE_DIRS}")
		if(EXISTS "${mbedTLS_CRYPTO_LIBRARY}")
			set_target_properties(mbedTLS::Crypto PROPERTIES
				IMPORTED_LOCATION "${mbedTLS_CRYPTO_LIBRARY}")
		endif()
	endif()

	if(NOT TARGET mbedTLS::X509)
		add_library(mbedTLS::X509 UNKNOWN IMPORTED)
		set_target_properties(mbedTLS::X509 PROPERTIES
			INTERFACE_INCLUDE_DIRECTORIES "${mbedTLS_INCLUDE_DIRS}")
		if(EXISTS "${mbedTLS_X509_LIBRARY}")
			set_target_properties(mbedTLS::X509 PROPERTIES
				IMPORTED_LOCATION "${mbedTLS_X509_LIBRARY}")
		endif()
	endif()
endif()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(mbedTLS DEFAULT_MSG
	mbedTLS_INCLUDE_DIR
	mbedTLS_LIBRARY
	mbedTLS_CRYPTO_LIBRARY
	mbedTLS_X509_LIBRARY
)

mark_as_advanced(
	mbedTLS_INCLUDE_DIR
	mbedTLS_LIBRARY
	mbedTLS_CRYPTO_LIBRARY
	mbedTLS_X509_LIBRARY
)
