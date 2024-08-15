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
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

// TODO - for java 11+ - import static java.nio.file.Files.readString;

public final class ResourceUtils {
    private static Properties properties;
    private static final HashMap<String, RunningJarTempFile> jarFileEntryTempMemStorage = new HashMap<>();
    private static final Logger logger = Logger.getLogger("ResourceUtils");

    public static void setProperties(Properties properties) {
        ResourceUtils.properties = properties;
    }

    public static String getProperty(String keyName) {
        String result = System.getenv(keyName);
        if(result == null || result.trim().isEmpty()) {
            result = System.getProperty(keyName);
        }
        if(result == null || result.trim().isEmpty()) {
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
                result.load(getContextClassLoader().getResourceAsStream(File.separator+"config.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(new FileInputStream(findFileInRunningJar("config.properties")));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("application.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(new FileInputStream(findFileInRunningJar("application.properties")));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream(File.separator+"application.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(new FileInputStream(findFileInRunningJar(File.separator+"application.properties")));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("app.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream(File.separator+"app.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(new FileInputStream(findFileInRunningJar("app.properties")));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("properties.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream(File.separator+"properties.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(new FileInputStream(findFileInRunningJar("properties.properties")));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("pom.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream(File.separator+"pom.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(new FileInputStream(findFileInRunningJar("pom.properties")));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            logger.warning( "Unable to load any form of properties file from resource directory");
        }
        return result;
    }

    public static Properties getProperties() {
        if(properties == null || properties.isEmpty()) {
            properties = loadPropertiesFile();
        }
        return properties;
    }

    public static String readFileIntoString(String path) throws IOException, URISyntaxException {
        return readFileIntoString(path, false);
    }

    private static String readFileIntoString(String path, boolean inRetry) throws IOException, URISyntaxException {
        String result = null;
        try {
            result = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            if(inRetry) {
                throw e;
            }
        }
        finally {
            if(!inRetry && result == null) {
                if (attemptRetryInChildDir(path)) {
                    result = readFileIntoString(path.replace(".."+File.separator, ""), true);
                } else {
                    result = readFileIntoString(".." + File.separator + path, true);
                }
            }
        }
        return result;
        // TODO - for java 11 - return readString(getFileFromPath(path).toPath());
    }

    public static JarFile getCurrentRunningJarFile() {
        JarFile jarFile = null;

        try {
            jarFile = new JarFile(getRunningJarPath(getCallerClassName()).toFile());
        } catch(Exception ignored) {}

        if(jarFile == null && getContextClassLoader().getClass().getProtectionDomain() != null && getContextClassLoader().getClass().getProtectionDomain().getCodeSource() != null)
        {
            try {
                URL jarUrl = getContextClassLoader().getClass().getProtectionDomain().getCodeSource().getLocation();
                if(jarUrl.getProtocol().equals("jar")) {
                    jarFile = new JarFile(new File(jarUrl.getPath()));
                } else {
                    jarFile = new JarFile(new File(jarUrl.toURI()));
                }
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
        return getFileFromPath(path, false);
    }

    private static File getFileFromPath(String path, boolean inRetry) throws URISyntaxException {
        File file = findFileInRunningJar(path);
        if(file == null) {
            URL resource = getContextClassLoader().getResource(path);
            if(resource != null) {
                if ("jar".equals(resource.getProtocol())) {
                    file = Files.exists(Paths.get(resource.getPath())) ? new File(resource.getPath()) : null;
                } else {
                    file = Files.exists(Paths.get(resource.toURI())) ? new File(resource.toURI()) : null;
                }
            }
        }
        if(file == null && Files.exists(Paths.get(path))) {
            file = new File(path);
        }

        // try one more time by checking child folder if "../" was used in path or checking parent folder if "../" was not used
        if(file == null && !inRetry) {
            if(attemptRetryInChildDir(path)) {
                return getFileFromPath(path.replace(".."+File.separator, ""), true);
            } else {
                return getFileFromPath(".."+File.separator+path, true);
            }
        }

        if(inRetry && file == null) {
            logger.warning("file not found! " + path);
        }

        return file;
    }

    public static List<File> getAllFilesInPath(String resourcePath, String filterByFileNameExtension) throws IOException, URISyntaxException {
        List<File> files = new ArrayList<>();

        getAllFileEntriesInRunningJar().values().stream()
                .filter(runningJarTempFile -> runningJarTempFile.getOriginalPath().equals(resourcePath))
                .forEach((runningJarTempFile) -> files.add(getResourceFile(runningJarTempFile.getFileEntry())));

        if(files.isEmpty()) {
            InputStream in = getContextClassLoader().getResourceAsStream(resourcePath);
            if (in != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                    String resource;
                    while ((resource = br.readLine()) != null) {
                        if ("*".equals(filterByFileNameExtension) || resource.endsWith(filterByFileNameExtension)) {
                            URL resourceFile = getContextClassLoader().getResource(resourcePath.endsWith(File.separator) ? resourcePath + resource : resourcePath + File.separator + resource);
                            if (resourceFile != null) {
                                files.add(new File(resourceFile.toURI()));
                            }
                        }
                    }
                } finally {
                    in.close();
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

    public static List<String> getAllFileNamesInPath(String path) {
        return getAllFileNamesInPath(path, false, "*");
    }

    public static List<String> getAllFileNamesInPath(String path, boolean checkSubDirectories) {
        return getAllFileNamesInPath(path, checkSubDirectories, "*");
    }

    public static List<String> getAllFileNamesInPath(String path, boolean checkSubDirectories, String filterByFileNameExtension) {
        List<String> filenames = new ArrayList<>();

        getAllFileEntriesInRunningJar().values().stream()
                .filter(runningJarTempFile -> (checkSubDirectories) ? runningJarTempFile.getOriginalPath().contains(path) : runningJarTempFile.getOriginalPath().equals(path))
                .forEach((runningJarTempFile) -> {
                    if ("*".equals(filterByFileNameExtension) || runningJarTempFile.getOriginalName().endsWith(filterByFileNameExtension)) {
                        filenames.add(runningJarTempFile.getOriginalName());
                    }
                });

        if(filenames.isEmpty()) {
            try (InputStream in = getContextClassLoader().getResource(path).openStream(); BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String resource;
                while ((resource = br.readLine()) != null) {
                    if ("*".equals(filterByFileNameExtension) || resource.endsWith(filterByFileNameExtension)) {
                        filenames.add(resource);
                    }
                }
            } catch (Exception ignore) {}
        }

        if(filenames.isEmpty()) {
            try (Stream<Path> filesInPath = Files.list(Paths.get(path))) {
                List<Path> filePaths = filesInPath.collect(Collectors.toList());
                for (Path filePath : filePaths) {
                    if ("*".equals(filterByFileNameExtension) || (filePath.getFileName() != null && filePath.getFileName().toString().endsWith(filterByFileNameExtension))) {
                        filenames.add(filePath.getFileName().toString());
                    }
                }
                filePaths.clear();
            } catch (IOException ignored) {}
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
        try {
            for (count = 0L; -1 != (n = inputStream.read(bufferBytes)); count += n) {
                outputStream.write(bufferBytes, 0, n);
            }
        } finally {
            inputStream.close();
            outputStream.close();
        }
        return count;
    }

    private static HashMap<String, RunningJarTempFile> getAllFileEntriesInRunningJar() {
        JarFile jar = getCurrentRunningJarFile();
        if(jar != null) {
            List<ZipEntry> entries = jar.stream()
                    .filter(jarEntry -> !jarEntry.isDirectory())
                    .collect(Collectors.toList());
            for (ZipEntry fileEntry : entries) {
                if (!jarFileEntryTempMemStorage.containsKey(fileEntry.getName())) {
                    String realName = null;
                    String pathName = "";
                    if (fileEntry.getName().substring(1).contains(File.separator)) {
                        realName = fileEntry.getName().substring(fileEntry.getName().lastIndexOf(File.separator));
                        pathName = fileEntry.getName().substring(0, fileEntry.getName().lastIndexOf(File.separator));
                    } else {
                        realName = fileEntry.getName();
                    }
                    if (realName.startsWith(File.separator)) {
                        realName = realName.substring(1);
                    }
                    jarFileEntryTempMemStorage.put(fileEntry.getName(), new RunningJarTempFile(pathName, realName, fileEntry));
                }
            }
        }
        return jarFileEntryTempMemStorage;
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

     public static Path getRunningJarPath() {
        return getRunningJarPath(getCallerClassName());
    }

    private static Path getRunningJarPath(String callerClassName) {
        Path result = null;
        try {
            result = Paths.get(Thread.currentThread()
                    .getContextClassLoader()
                    .loadClass(callerClassName)
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
        } catch(ClassNotFoundException | URISyntaxException ignored) {}
        return result;
    }

    private static String getCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String callerClassName = null;
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(ResourceUtils.class.getName())&& ste.getClassName().indexOf("java.lang.Thread")!=0) {
                if (callerClassName==null) {
                    callerClassName = ste.getClassName();
                } else if (!callerClassName.equals(ste.getClassName())) {
                    return ste.getClassName();
                }
            }
        }
        return null;
    }

    public static String getRunningJarDirectory() {
        Path runningJarPath = getRunningJarPath(getCallerClassName());
        return runningJarPath != null ? runningJarPath.toString().substring(0,runningJarPath.toString().lastIndexOf(File.separator)) : null;
    }

    private static boolean attemptRetryInChildDir(String path) {
        // try one more time by checking child folder if "../" was used in path or checking parent folder if "../" was not used
        return (path.startsWith("..") || path.startsWith(File.separator+"..") || path.startsWith("."+File.separator+".."));
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
