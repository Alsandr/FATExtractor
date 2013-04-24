package asandrc.gmail.com.extractor;

import asandrc.gmail.com.data.FAT32DIRElement;
import java.io.File;

/**
 *
 * @author Саша
 */
public abstract class BaseExtractor {
    
    public abstract void openFAT(File FATfile);
    
    public abstract byte[] extractFile(FAT32DIRElement f32DIRElement);
    
}
