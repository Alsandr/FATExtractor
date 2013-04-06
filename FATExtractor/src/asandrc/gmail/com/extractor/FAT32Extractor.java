package asandrc.gmail.com.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FAT32Extractor  extends BaseExtractor{
    
    private byte[] imageBytes;
    
    private String BS_OEMNAme;
    private String BPB_BytsPerSec;
    private String BPB_SecPerClus;
    private String BPB_NumFATs;
    private String BPB_TotSec32;
    private String BPB_FATSz16;
    private String BPB_FATSz32;
    private String BPB_FSVer;
    
    private File image;
    private FileInputStream fs;
    
    @Override
    public void openFAT(String path) {
        image = new File(path);
        if (image.exists()) {
            try {
                fs = new FileInputStream(image);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FAT32Extractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("File doesn't exsist");
        }
    }
    
    public void init() throws IOException {
        getImageBytes();
        getBS_OEMNAME();
        getBPB_BytsPerSec();
        getBPB_SecPerClus();
        getBPB_NumFATs();
        getBPB_FATSz16();
        getBPB_TotSec32();
        getBPB_FATSz32();
        getBPB_FSVer();
    }
    
    private void getImageBytes() throws IOException {
        imageBytes = new byte[102400000];
        fs.read(imageBytes, 0, 102400000);
    }
    
    private void getBS_OEMNAME() throws IOException {
        byte[] bTemp = new byte[8];
        for (int i = 3;i < 11; i++) {
            bTemp[i-3] = imageBytes[i];
        }        
        BS_OEMNAme = new String(bTemp, "UTF-8");
        System.out.println(BS_OEMNAme);        
        
//        int n;
//        int c = 0;
//        
//        for (byte b: imageBytes) {
//            n = b;            
//            System.out.println("[" + c + "]:" + Integer.toHexString(n));
//            c++;
//        }        
    }
    
    private void getBPB_BytsPerSec() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 11;i < 13; i++) {
            bTemp[i-11] = imageBytes[i];
        }        
        BPB_BytsPerSec = new String(bTemp, "UTF-8");
        System.out.println(BPB_BytsPerSec); 
    }
    
    private void getBPB_SecPerClus() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[1];
        for (int i = 13;i < 13; i++) {
            bTemp[i-13] = imageBytes[i];
        }        
        BPB_SecPerClus = new String(bTemp, "UTF-8");
        System.out.println(BPB_SecPerClus); 
    }
    
    private void getBPB_NumFATs() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[1];
        for (int i = 16;i < 16; i++) {
            bTemp[i-16] = imageBytes[i];
        }        
        BPB_NumFATs = new String(bTemp, "UTF-8");
        System.out.println(BPB_NumFATs); 
    }
    
    private void getBPB_FATSz16() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 22;i < 24; i++) {
            bTemp[i-22] = imageBytes[i];
        }        
        BPB_FATSz16 = new String(bTemp, "UTF-8");
        System.out.println(BPB_FATSz16); 
    }
    
    private void getBPB_TotSec32() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 32;i < 36; i++) {
            bTemp[i-32] = imageBytes[i];
        }        
        BPB_TotSec32 = new String(bTemp, "UTF-8");
        System.out.println(BPB_TotSec32); 
    }
    
    private void getBPB_FATSz32() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 36;i < 40; i++) {
            bTemp[i-36] = imageBytes[i];
        }        
        BPB_FATSz32 = new String(bTemp, "UTF-8");
        System.out.println(BPB_FATSz32); 
    }
    
    private void getBPB_FSVer() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 42;i < 44; i++) {
            bTemp[i-42] = imageBytes[i];
        }        
        BPB_FSVer = new String(bTemp, "UTF-8");
        System.out.println(BPB_FSVer); 
    }
    
    @Override
    public void getFiles(String path) {
        
    }
    
    @Override
    public void getFile(String path) {
        
    }
}
