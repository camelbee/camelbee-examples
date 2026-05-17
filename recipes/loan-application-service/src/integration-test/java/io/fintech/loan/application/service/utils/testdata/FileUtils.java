package io.fintech.loan.application.service.utils.testdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utility class for handling file operations including copying and moving files.
 */
public final class FileUtils {

  private FileUtils() {
    // Private constructor to prevent instantiation
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Copies a file from source location to destination with specified file names.
   *
   * @param sourceFolder   The folder containing the source file
   * @param sourceFileName The name of the source file (including extension)
   * @param destFolder     The destination folder path
   * @param destFileName   The name for the destination file (including extension)
   * @throws IOException if an I/O error occurs
   */
  public static void copyFile(String sourceFolder, String sourceFileName,
      String destFolder, String destFileName) throws IOException {
    try {
      // Create destination directory if it doesn't exist
      Files.createDirectories(Paths.get(destFolder));

      // Set up source and destination paths
      Path sourcePath = Paths.get(sourceFolder, sourceFileName);
      Path destPath = Paths.get(destFolder, destFileName);

      // Copy the file, replacing if it already exists
      Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
      System.out.printf("✓ File copied from %s to %s%n", sourcePath, destPath);

    } catch (IOException e) {
      System.err.printf("✗ Error copying file from %s to %s: %s%n",
          sourceFileName, destFileName, e.getMessage());
      throw e;
    }
  }

  /**
   * Verifies if the specified file exists in the given folder.
   *
   * @param folder   The folder to check
   * @param fileName The name of the file to check
   * @return boolean indicating if the file exists
   */
  public static boolean fileExists(String folder, String fileName) {
    Path filePath = Paths.get(folder, fileName);
    return Files.exists(filePath);
  }

  /**
   * Creates directories if they don't exist.
   *
   * @param directoryPath The path of directory to create
   * @throws IOException if an I/O error occurs
   */
  public static void createDirectoryIfNotExists(String directoryPath) throws IOException {
    Path path = Paths.get(directoryPath);
    if (!Files.exists(path)) {
      Files.createDirectories(path);
    }
  }

  /**
   * Moves a file from one location to another.
   *
   * @param sourceFolder   Source folder path
   * @param sourceFileName Source file name
   * @param destFolder     Destination folder path
   * @param destFileName   Destination file name
   * @throws IOException if an I/O error occurs
   */
  public static void moveFile(String sourceFolder, String sourceFileName,
      String destFolder, String destFileName) throws IOException {
    Path sourcePath = Paths.get(sourceFolder, sourceFileName);
    Path destPath = Paths.get(destFolder, destFileName);

    createDirectoryIfNotExists(destFolder);
    Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
  }

  /**
   * Deletes a file if it exists.
   *
   * @param folder   Folder containing the file
   * @param fileName Name of the file to delete
   * @return true if file was deleted, false if file didn't exist
   * @throws IOException if an I/O error occurs
   */
  public static boolean deleteFile(String folder, String fileName) throws IOException {
    Path path = Paths.get(folder, fileName);
    return Files.deleteIfExists(path);
  }
}