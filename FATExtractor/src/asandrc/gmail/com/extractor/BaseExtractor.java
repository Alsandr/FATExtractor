package asandrc.gmail.com.extractor;

import java.io.File;
import java.util.List;

public abstract class BaseExtractor {
    
    public abstract void openFAT(String path);
    
    public abstract List<File> getFiles(String path);
    
    public abstract File getFile(String path);    
    
}
