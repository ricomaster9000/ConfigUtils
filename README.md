# ResourceUtils
Just a small library that wraps some logic for accessing files and data in the usual resources directory of current jar

add as dependency by using jitpack.io, go to this link: https://jitpack.io/#ricomaster9000/ResourceUtils/1.0.0

### methods available:

    public static String getProperty(String keyName)

    public static String getResourceProperty(String keyName)

    public static Properties loadPropertiesFile() // loads the properties file as it is saved in the resource dir

    public static Properties getProperties() // gets the file from the stored private static variable inside the class, it only gets the resource file date       using loadPropertiesFile once

    public String readFileIntoString(File file)

    public String readFileIntoString(Path path)

    public String readFileIntoString(URL resource)

    public String readFileIntoString(String path)

    public static File getFileFromPath(String path) throws URISyntaxException

    public List<File> getAllFilesByRegexPattern(String dirPath, String regexPattern)

    public List<File> getAllFilesByRegexPattern(Path dirPath, String regexPattern)

    public static List<String> getAllFileNamesInPath(String path, boolean alsoCheckSubDirectories) throws IOException

    public static InputStream getResourceAsStream(String resource)

    public static Boolean doesDirectoryOrFileExistInDirectory(String directory)

