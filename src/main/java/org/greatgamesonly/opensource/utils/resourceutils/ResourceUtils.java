package org.greatgamesonly.opensource.utils.resourceutils;


import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static java.nio.file.Files.readString;

public final class ResourceUtils {
    private static Properties properties;
    private static final HashMap<String, RunningJarTempFile> jarFileEntryTempMemStorage = new HashMap<>();

    public static String getProperty(String keyName) {
        String result = System.getenv(keyName);
        if(result == null || result.isBlank()) {
            result = getProperties().getProperty(keyName);
        }
        return result;
    }

    public static String getResourceProperty(String keyName) {
        return getProperties().getProperty(keyName);
    }

    public static Properties loadPropertiesFile() {
        Properties result = new Properties();
        try {
            result.load(getContextClassLoader().getResourceAsStream("config.properties"));
        } catch (Exception ignore) {}
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("application.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("app.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("properties.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("properties.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            throw new RuntimeException("Unable to load any form of properties file from resource directory");
        }
        return result;
    }

    public static Properties getProperties() {
        if(properties == null || properties.isEmpty()) {
            properties = loadPropertiesFile();
        }
        return properties;
    }

    // handles the part to verify file actually exists
    public static String readFileIntoString(String path) throws IOException, URISyntaxException {
        return readString(getFileFromPath(path).toPath());
    }

    public static JarFile getCurrentRunningJarFile() {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(System.getProperty("java.class.path"));
        } catch(Exception ignored) {}

        if(jarFile == null && getContextClassLoader().getClass().getProtectionDomain() != null && getContextClassLoader().getClass().getProtectionDomain().getCodeSource() != null)
        {
            try {
                jarFile = new JarFile(getContextClassLoader().getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
            } catch (Exception ignored) {}
        }
        if(jarFile == null) {
            try {
                jarFile = new JarFile("application.jar");
            } catch (Exception ignored) {}
        }
        if(jarFile == null) {
            try {
                jarFile = new JarFile("app.jar");
            } catch (Exception ignored) {}
        }
        return jarFile;
    }

    //"*.{java,class,jar}"
    // TODO - finish
    private static List<File> getAllFilesByRegexPattern(String dirPath, String regexPattern) {
        return getAllFilesByRegexPattern(Paths.get(dirPath), regexPattern);
    }

    // TODO - finish
    private static List<File> getAllFilesByRegexPattern(Path dirPath, String regexPattern) {
        List<File> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, regexPattern)) {
            for (Path entry : stream) {
                files.add(entry.toFile());
            }
            return files;
        } catch (IOException x) {
            throw new RuntimeException(String.format("error reading directory %s: %s", dirPath, x.getMessage()), x);
        }
    }

    public static File getFileFromPath(String path) throws URISyntaxException {
        File file = findFileInRunningJar(path);
        if(file == null) {
            URL resource = getContextClassLoader().getResource(path);
            if (resource == null) {
                throw new IllegalArgumentException("file not found! " + path);
            } else {
                return new File(resource.toURI());
            }
        }
        return file;
    }

    public static List<File> getAllFilesInPath(String resourcePath, String filterByFileNameExtension) throws IOException, URISyntaxException {
        List<File> files = new ArrayList<>();

        getAllFileEntriesInRunningJar().values().stream()
                .filter(runningJarTempFile -> runningJarTempFile.getOriginalPath().equals(resourcePath))
                .forEach((runningJarTempFile) -> files.add(getResourceFile(runningJarTempFile.getFileEntry())));

        if(files.isEmpty()) {
            try (InputStream in = getContextClassLoader().getResourceAsStream(resourcePath); BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String resource;
                while ((resource = br.readLine()) != null) {
                    if (resource.endsWith(filterByFileNameExtension)) {
                        URL resourceFile = getContextClassLoader().getResource(resourcePath.endsWith("/") ? resourcePath + resource : resourcePath + "/" + resource);
                        if (resourceFile != null) {
                            files.add(new File(resourceFile.toURI()));
                        }
                    }
                }
            }
        }
        return files;
    }

    public static File getResourceFile(ZipEntry fileEntry) {
        File tempFile = null;
        try {
            InputStream input = getCurrentRunningJarFile().getInputStream(fileEntry);
            int dotIndexFileExt = fileEntry.getName().lastIndexOf('.');
            if (dotIndexFileExt > 0) {
                tempFile = File.createTempFile(fileEntry.getName().substring(0, dotIndexFileExt), fileEntry.getName().substring(dotIndexFileExt));
            } else {
                tempFile = File.createTempFile(fileEntry.getName(), null);
            }
            tempFile.deleteOnExit();
            FileOutputStream out = new FileOutputStream(tempFile);
            copyLarge(input, out);
        } catch (IOException ignored) {}
        return tempFile;
    }

    public static List<String> getAllFileNamesInPath(String path, boolean checkSubDirectories) {
        List<String> filenames = new ArrayList<>();

        getAllFileEntriesInRunningJar().values().stream()
                .filter(runningJarTempFile -> (checkSubDirectories) ? runningJarTempFile.getOriginalPath().contains(path) : runningJarTempFile.getOriginalPath().equals(path))
                .forEach((runningJarTempFile) -> filenames.add(runningJarTempFile.getOriginalName()));

        if(filenames.isEmpty()) {
            try (InputStream in = getContextClassLoader().getResource(path).openStream(); BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String resource;
                while ((resource = br.readLine()) != null) {
                    filenames.add(resource);
                }
            } catch (Exception ignored) {}
        }
        return filenames;
    }

    public File findFileInRunningJar(URL fullPath) {
        return findFileInRunningJar(fullPath.getPath());
    }

    public File findFileInRunningJar(Path fullPath) {
        return findFileInRunningJar(fullPath.toString());
    }

    public static Boolean doesDirectoryOrFileExist(String path) {
        boolean doesExist = getCurrentRunningJarFile().getEntry(path) != null;
        if(!doesExist) {
            final URL resource = getContextClassLoader().getResource(path);
            doesExist = (resource != null && resource.getPath() != null);
        }
        return doesExist;
    }

    public static File findFileInRunningJar(String fullPath) {
        RunningJarTempFile file = getAllFileEntriesInRunningJar().containsKey(fullPath) ?
                getAllFileEntriesInRunningJar().get(fullPath) :
                getAllFileEntriesInRunningJar().values().stream()
                        .filter(runningJarTempFile -> runningJarTempFile.getOriginalPath().contains(fullPath))
                        .findFirst().orElse(null);
        return file != null ? getResourceFile(file.getFileEntry()) : null;
    }

    public static long copyLarge(InputStream inputStream, OutputStream outputStream) throws IOException {
        return copyLarge(inputStream, outputStream, 8192);
    }

    public static long copyLarge(InputStream inputStream, OutputStream outputStream, int buffer) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        Objects.requireNonNull(outputStream, "outputStream");
        byte[] bufferBytes = new byte[buffer];
        long count;
        int n;
        for(count = 0L; -1 != (n = inputStream.read(bufferBytes)); count += n) {
            outputStream.write(bufferBytes, 0, n);
        }
        inputStream.close();
        outputStream.close();
        return count;
    }

    private static HashMap<String, RunningJarTempFile> getAllFileEntriesInRunningJar() {
        JarFile jar = getCurrentRunningJarFile();
        List<ZipEntry> entries = jar.stream()
                .filter(jarEntry -> !jarEntry.isDirectory())
                .collect(Collectors.toList());
        for(ZipEntry fileEntry : entries) {
            if(!jarFileEntryTempMemStorage.containsKey(fileEntry.getName())) {
                String realName = null;
                String pathName = "";
                if (fileEntry.getName().substring(1).contains("/")) {
                    realName = fileEntry.getName().substring(fileEntry.getName().lastIndexOf("/"));
                    pathName = fileEntry.getName().substring(0, fileEntry.getName().lastIndexOf("/"));
                } else {
                    realName = fileEntry.getName();
                }
                if(realName.startsWith("/")) {
                    realName = realName.substring(1);
                }
                jarFileEntryTempMemStorage.put(fileEntry.getName(),new RunningJarTempFile(pathName, realName, fileEntry));
            }
        }
        return jarFileEntryTempMemStorage;
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static class RunningJarTempFile {
        private final String originalPath;
        private final String originalName;

        private final ZipEntry fileEntry;

        public RunningJarTempFile(String originalPath, String originalName, ZipEntry fileEntry) {
            this.originalPath = originalPath;
            this.originalName = originalName;
            this.fileEntry = fileEntry;
        }

        public String getOriginalPath() {
            return originalPath;
        }

        public String getOriginalName() {
            return originalName;
        }

        public ZipEntry getFileEntry() {
            return fileEntry;
        }
    }
}
