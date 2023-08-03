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
    }

    @AfterClass
    public static void PostTestClassRun() {
        System.out.println("TESTS - CLEAN UP DATA");
        System.out.println("TESTS - END");
    }
}