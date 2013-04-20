package asandrc.gmail.com.extractor;

public abstract class BaseExtractor {
    
    public abstract void openFAT(String path);
    
    public abstract void getFiles();
    
    public abstract void getFile(String path);    
    
}
