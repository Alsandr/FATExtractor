package com.gmail.asandrc.data;

import java.io.UnsupportedEncodingException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Als
 */
public class FAT32DIRElementTest {
    
    FAT32DIRElement probeElement;
    
    public FAT32DIRElementTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws UnsupportedEncodingException {
        probeElement = new FAT32DIRElement(new byte[32]);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void FAT32DIRElementEqualToDIR_ELEMENT_SIZE() {
        assertEquals((int)probeElement.DIR_ELEMENT_SIZE, probeElement.bytesOfFAT32Element.length);
    }
    
}
