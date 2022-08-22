# ResourceUtils
Just a small library that wraps some logic for accessing files and data in the usual resources directory of current jar

add as dependency by using jitpack.io, go to this link: https://jitpack.io/#ricomaster9000/ResourceUtils/1.0.2.1

### methods available:

    public static String getProperty(String keyName)

    public static String getResourceProperty(String keyName)

    public static Properties loadPropertiesFile() // loads the properties file as it is saved in the resource dir

    public static Properties getProperties() // gets the file from the stored private static variable inside the class, it only gets the resource file date       using loadPropertiesFile once

    public static String readFileIntoString(File file)

    public static String readFileIntoString(Path path)

    public static String readFileIntoString(URL resource)

    public static String readFileIntoString(String path)

    public static File getFileFromPath(String path) throws URISyntaxException

    public static List<File> getAllFilesByRegexPattern(String dirPath, String regexPattern)

    public static List<File> getAllFilesByRegexPattern(Path dirPath, String regexPattern)

    public static List<String> getAllFileNamesInPath(String path, boolean alsoCheckSubDirectories) throws IOException

    public static InputStream getResourceAsStream(String resource)

    public static Boolean doesDirectoryOrFileExistInDirectory(String directory)

