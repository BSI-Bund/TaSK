package com.achelos.task.utilities;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Helper class for utility functions regarding files.
 */
public class FileUtils {
	private FileUtils() {
		// Hidden Constructor.
	}

	/**
	 * Generate the SHA256 Fingerprint of the provided file.
	 * @param fileToHash The file to generate the fingerprint for.
	 * @return The SHA256 Fingerprint of the provided file.
	 */
	public static byte[] getFileFingerprint(final File fileToHash) {
		return getFileFingerprint(fileToHash, "SHA-256");
	}
	/**
	 * Generate a Fingerprint of the provided file.
	 * @param fileToHash The file to generate the fingerprint for.
	 * @param hashFunction The name of the hashfunction to use.
	 * @return The hashfunction Fingerprint of the provided file.
	 */
	public static byte[] getFileFingerprint(final File fileToHash, final String hashFunction) {
		if (fileToHash == null) {
			return new byte[] { };
		}
		try (var fileInputStream = new FileInputStream(fileToHash)){
			var digest = MessageDigest.getInstance(hashFunction);

			byte[] buffer = new byte[1024];
			int bytesCount = 0;

			while ((bytesCount = fileInputStream.read(buffer)) != -1) {
				digest.update(buffer, 0, bytesCount);
			}

			fileInputStream.close();

			// Get the hash's bytes
			return digest.digest();
		} catch (Exception e) {
			throw new RuntimeException("Unable to calculate fingerprint of file: " + fileToHash.getAbsolutePath(), e);
		}
	}

	/**
	 * Zip the directoryToZip into the destination file.
	 * @param directoryToZip The directory to zip.
	 * @param destination The path the zipped folder should be written into.
	 * @throws IOException If an IO error occurs.
	 */
	public static void zipFolder(final File directoryToZip, final File destination) throws IOException {
		var tmpDir = Files.createTempDirectory("task");
		var tmpZipFile = tmpDir.resolve(destination.getName());
		var zipOutputStream = new ZipOutputStream(new FileOutputStream(tmpZipFile.toFile()));
		Files.walkFileTree(directoryToZip.toPath(), new SimpleFileVisitor<>() {
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				zipOutputStream.putNextEntry(new ZipEntry(directoryToZip.toPath().relativize(file).toString()));
				Files.copy(file, zipOutputStream);
				zipOutputStream.closeEntry();
				return FileVisitResult.CONTINUE;
			}
		});
		zipOutputStream.close();

		//
		Files.move(tmpZipFile, destination.toPath());
	}
}
