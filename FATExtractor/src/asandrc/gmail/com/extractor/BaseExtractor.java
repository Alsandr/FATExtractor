package asandrc.gmail.com.extractor;

import java.io.File;

/**
 *
 * @author Саша
 */
public abstract class BaseExtractor {
    
    public abstract void openFAT(File FATfile);
    
    public abstract void getFiles();
    
    public abstract void getFile(String path);    
    
}
