
find_path(ZLIB_INCLUDE_DIR
    zlib.h
    HINTS ${EP_INCLUDEDIR}
)
find_library(ZLIB_LIBRARY
    z
    HINTS ${EP_LIBDIR}
)

if(ZLIB_INCLUDE_DIR AND ZLIB_LIBRARY)
	set(ZLIB_INCLUDE_DIRS ${ZLIB_INCLUDE_DIR})
	set(ZLIB_LIBRARIES ${ZLIB_LIBRARY})

	if(NOT TARGET ZLIB::ZLIB)
		add_library(ZLIB::ZLIB UNKNOWN IMPORTED)
		set_target_properties(ZLIB::ZLIB PROPERTIES
			INTERFACE_INCLUDE_DIRECTORIES "${ZLIB_INCLUDE_DIRS}")
		if(EXISTS "${ZLIB_LIBRARY}")
			set_target_properties(ZLIB::ZLIB PROPERTIES
				IMPORTED_LOCATION "${ZLIB_LIBRARY}")
		endif()
	endif()
endif()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(zlib DEFAULT_MSG
	ZLIB_INCLUDE_DIR
	ZLIB_LIBRARY
)

mark_as_advanced(
	ZLIB_INCLUDE_DIR
	ZLIB_LIBRARY
)

