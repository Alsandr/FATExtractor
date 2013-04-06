package asandrc.gmail.com.extractor;

import java.io.File;
import java.util.List;

public abstract class BaseExtractor {
    
    public abstract void openFAT(String path);
    
    public abstract void getFiles(String path);
    
    public abstract void getFile(String path);    
    
}
