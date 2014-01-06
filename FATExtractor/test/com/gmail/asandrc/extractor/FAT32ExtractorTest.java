package com.gmail.asandrc.extractor;

import com.gmail.asandrc.data.FAT32DIRElement;
import com.gmail.asandrc.data.FAT32Directory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Als
 */
public class FAT32ExtractorTest {
    
    private final static String PATH = "D:/imageFAT32.fat32";
    
    private FAT32Extractor f32Extractor;
    
    public FAT32ExtractorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        f32Extractor = new FAT32Extractor();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of openFAT method, of class FAT32Extractor.
     * Test open file(image/file system) FAT32
     */
    @Test
    public void testOpenFAT() {
        System.out.println("openFAT");
        File FATfile = new File(PATH);
        boolean expResult = true;
        boolean result = f32Extractor.openFAT(FATfile);
        assertEquals(expResult, result);
    }
    
    /**
     * Testing search for FAT32Element in an open image(file)
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testSearchFAT32Element() throws UnsupportedEncodingException {
        System.out.println("searchFAT32Element");
        boolean expResult = true;
        f32Extractor.openFAT(new File(PATH));
        boolean result = f32Extractor.searchFAT32Element();
        assertEquals(expResult, result);
    }
    
    /**
     * Test readCluster (read 0-cluster) for empty cluster
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testReadCluster() throws UnsupportedEncodingException {
        System.out.println("extractFile");
        boolean expResult = true;
        boolean result = false;
        f32Extractor.openFAT(new File(PATH));
        f32Extractor.searchFAT32Element();
        
        byte[] actualArr = f32Extractor.readCluster(0);
        for (int i = 0; i < actualArr.length - 1; i++) {
            if (actualArr[i] != 0)
                result = true;
        }
        assertEquals(expResult, result);
    }
}
