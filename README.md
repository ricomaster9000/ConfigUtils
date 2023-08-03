# ResourceUtils
Just a small library that wraps some logic for accessing files and data in the usual resources directory of current jar and from the environment (ENV vars)

add as dependency by using jitpack.io, go to this link: https://jitpack.io/#ricomaster9000/ResourceUtils/1.1.3

### methods available:

    public static String getProperty(String keyName)

    public static String getResourceProperty(String keyName)

    public static Properties loadPropertiesFile()

    public static Properties getProperties()

    public static String readFileIntoString(String path) throws IOException, URISyntaxException

    public static JarFile getCurrentRunningJarFile()

    public static File getFileFromPath(String path) throws URISyntaxException

    public static List<File> getAllFilesInPath(String resourcePath, String filterByFileNameExtension) throws IOException, URISyntaxException

    public static List<String> getAllFileNamesInPath(String path, boolean checkSubDirectories) throws IOException

    public File findFileInRunningJar(URL fullPath)

    public File findFileInRunningJar(Path fullPath)

    public static Boolean doesDirectoryOrFileExist(String path)

    public static File findFileInRunningJar(String fullPath)

    public static long copyLarge(InputStream inputStream, OutputStream outputStream)

    public static long copyLarge(InputStream inputStream, OutputStream outputStream, int buffer) throws IOException

    public static String getRunningJarDirectory()

    public static Path getRunningJarPath()

