package org.greatgamesonly.opensource.utils.resourceutils;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class MainTest {


    @BeforeClass
    public static void setupEnvironment() {
        System.out.println("TESTS - Setting up repositories&connection-pools and establishing connection");
        System.out.println("TESTS - BEGIN");
    }

    @Test()
    public void testReadFileIntoString() throws NoSuchFieldException, IllegalAccessException, IOException, URISyntaxException {
        System.out.println("TESTS - read whole resource files into string");

        Assert.assertEquals("readFileToString 1 - file contents must be correct", "Hello Test 1", ResourceUtils.readFileIntoString("resourceTestFiles/testResourceFile.txt"));
        Assert.assertEquals("readFileToString 2 - file contents must be correct", "<html><body><div>Hello Test 2</div></body></html>", ResourceUtils.readFileIntoString("resourceTestFiles/testResourceFile2.html"));
        Assert.assertEquals("readFileToString 3 - file contents must be correct", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ResourceUtils.readFileIntoString("resourceTestFiles/testResourceFile3.xml"));

        Assert.assertEquals("readFileToString 4 triggerRetry - file contents must be correct", "Hello Test 1", ResourceUtils.readFileIntoString("../resourceTestFiles/testResourceFile.txt"));
    }

    @Test()
    public void testGetFileFromPath() throws IOException, URISyntaxException {
        System.out.println("TESTS - get file from path");

        Assert.assertTrue("getFileFromPath 1 - file must exist and not be empty", ResourceUtils.getFileFromPath("resourceTestFiles/testResourceFile.txt").exists());
        Assert.assertTrue("getFileFromPath 2 triggerRetry - file must exist and not be empty", ResourceUtils.getFileFromPath("../resourceTestFiles/testResourceFile.txt").exists());
    }

    @Test()
    public void testGetAllFileNamesInPath() throws IOException, URISyntaxException {
        System.out.println("TESTS - get all full filenames in path");

        Assert.assertTrue("GetAllFileNamesInPath 1 - must return a list of filenames bigger than 2 and which contains correct name", ResourceUtils.getAllFileNamesInPath("resourceTestFiles").size() >= 3 && ResourceUtils.getAllFileNamesInPath("resourceTestFiles").contains("testResourceFile2.html"));

        Assert.assertTrue("GetAllFileNamesInPath 2 - must return a list of filenames filtered correctly",
                ResourceUtils.getAllFileNamesInPath("resourceTestFiles", false, ".xml")
                        .stream()
                        .allMatch(fileName -> fileName.endsWith(".xml")) &&
                        !ResourceUtils.getAllFileNamesInPath("resourceTestFiles", false, ".xml").isEmpty()
        );
    }

    @AfterClass
    public static void PostTestClassRun() {
        System.out.println("TESTS - CLEAN UP DATA");
        System.out.println("TESTS - END");
    }
}
