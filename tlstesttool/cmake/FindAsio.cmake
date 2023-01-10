
find_path(Asio_INCLUDE_DIR
	asio.hpp
)

if(Asio_INCLUDE_DIR)
	set(Asio_INCLUDE_DIRS ${Asio_INCLUDE_DIR})

	if(NOT TARGET Asio::Asio)
		add_library(Asio::Asio INTERFACE IMPORTED)
		list(APPEND Asio_FLAGS ASIO_STANDALONE ASIO_DISABLE_THREADS ASIO_NO_DEPRECATED)
		set(Asio_LIBS "")
		set_target_properties(Asio::Asio PROPERTIES
			INTERFACE_COMPILE_DEFINITIONS "${Asio_FLAGS}")
		set_target_properties(Asio::Asio PROPERTIES
			INTERFACE_INCLUDE_DIRECTORIES "${Asio_INCLUDE_DIRS}")
		set_target_properties(Asio::Asio PROPERTIES
			INTERFACE_LINK_LIBRARIES "${Asio_LIBS}")
	endif()
endif()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Asio DEFAULT_MSG
	Asio_INCLUDE_DIR
)

mark_as_advanced(
	Asio_INCLUDE_DIR
)
