package asandrc.gmail.com.main;

import asandrc.gmail.com.extractor.FAT32Extractor;
import java.io.IOException;

public class FATExtractor {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        FAT32Extractor f32Extractor = new FAT32Extractor();
        f32Extractor.openFAT("D:/imageFAT32.fat32");
        f32Extractor.init();        
    }
}
