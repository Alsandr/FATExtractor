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
    private Integer BPB_BytsPerSec;
    private Integer BPB_SecPerClus;
    private Integer BPB_ReservedSecCnt;
    private Integer BPB_NumFATs;
    private Integer BPB_TotSec32;
    private Integer BPB_FATSz16;
    private Integer BPB_FATSz32;
    private Integer BPB_FSVer;
    private Integer BPB_RootClus;
    private String BPB_FilSysType;
    private Integer BPB_RootEntCnt;
    private Integer FirstDataSector;
    
    private Integer RootDirSectors;
    
    private File image;
    private FileInputStream fs;
    
    /**
     * Метод открытия файла (образа) файловой системы
     * @param path путь к файлу (образу)
     */
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
    
    /**
     * Метод инициализации FAT32Extractor-а
     * получение байтов файла (образа) и информации о файловой системе
     * @throws IOException 
     */
    
    public void init() throws IOException {
        getImageBytes();
        getBS_OEMNAME();
        getBPB_BytsPerSec();
        getBPB_SecPerClus();
        getBPB_ReservedSecCnt();
        getBPB_NumFATs();
        getBPB_FATSz16();
        getBPB_RootEntCnt();
        getBPB_TotSec32();
        getBPB_FATSz32();
        getBPB_FSVer();
        getBPB_RootClus();
        getBPB_FilSysType();
        calcRootDirSectors();
        firstDataSector();
    }
    
    /**
     * Получить байты файла (образа)
     * @throws IOException 
     */    
    private void getImageBytes() throws IOException {
        imageBytes = new byte[102400000];
        fs.read(imageBytes, 0, 102400000);
    }
    
    /**
     * Преобразует массив байтов в целое ччисло
     * @param bytes
     * @return 
     */
    private int byteArrayToInt(byte[] bytes) {
        int result = 0;
        int l = bytes.length - 1;
        for (int i = 0; i < bytes.length; i++) {
            if (i == l) result += bytes[i] << i * 8;
            else result += (bytes[i] & 0xFF) << i * 8;            
        }
        return result;
    }
    
    /**
     * Получение строки имени системы, какой был отформатирована файловая система
     * @throws IOException 
     */
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
    
    /**
     * Получение количества байтов в секторе
     * @throws UnsupportedEncodingException 
     */
    private void getBPB_BytsPerSec() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 11;i < 13; i++) {
            bTemp[i-11] = imageBytes[i];
        }        
        BPB_BytsPerSec = byteArrayToInt(bTemp);
        System.out.println(BPB_BytsPerSec); 
    }
    
    /**
     * Получение количества секторов в кластере
     * @throws UnsupportedEncodingException 
     */
    private void getBPB_SecPerClus() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[1];
        for (int i = 13;i < 13; i++) {
            bTemp[i-13] = imageBytes[i];
        }        
        BPB_SecPerClus = byteArrayToInt(bTemp);
        System.out.println(BPB_SecPerClus); 
    }
    
    /**
     * Получение количества секторов в Reserved region
     * @throws UnsupportedEncodingException 
     */
    private void getBPB_ReservedSecCnt() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 14;i < 16; i++) {
            bTemp[i-14] = imageBytes[i];
        }        
        BPB_ReservedSecCnt = byteArrayToInt(bTemp);
        System.out.println(BPB_ReservedSecCnt); 
    }
    
    /**
     * Получение количесвта FAT таблиц на диске
     * @throws UnsupportedEncodingException 
     */
    private void getBPB_NumFATs() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[1];
        for (int i = 16;i < 17; i++) {
            bTemp[i-16] = imageBytes[i];
        }        
        BPB_NumFATs = byteArrayToInt(bTemp);
        System.out.println(BPB_NumFATs); 
    }
    
    private void getBPB_RootEntCnt() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 17;i < 19; i++) {
            bTemp[i-17] = imageBytes[i];
        }        
        BPB_RootEntCnt = byteArrayToInt(bTemp);
        System.out.println(BPB_RootEntCnt); 
    }
    
    /**
     * Получение количества секторов одной FAT (для FAT12/FAT16)
     * @throws UnsupportedEncodingException 
     */
    private void getBPB_FATSz16() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 22;i < 24; i++) {
            bTemp[i-22] = imageBytes[i];
        }        
        BPB_FATSz16 = byteArrayToInt(bTemp);
        System.out.println(BPB_FATSz16); 
    }
    
    /**
     * Получение общего количества секторов на диске (для FAT32)
     * @throws UnsupportedEncodingException 
     */
    private void getBPB_TotSec32() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 32;i < 36; i++) {
            bTemp[i-32] = imageBytes[i];
        }        
        BPB_TotSec32 = byteArrayToInt(bTemp);
        System.out.println(BPB_TotSec32); 
    }
    
    /**
     * Получение количества секторов одной FAT (для FAT32)
     * @throws UnsupportedEncodingException 
     */
    private void getBPB_FATSz32() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 36;i < 40; i++) {
            bTemp[i-36] = imageBytes[i];
        }        
        BPB_FATSz32 = byteArrayToInt(bTemp);
        System.out.println(BPB_FATSz32); 
    }
    
    /**
     * Получение венрсии FAT32 (старший байт - номер версии, 
     * младший - номер промежуточной версии)
     * @throws UnsupportedEncodingException 
     */
    private void getBPB_FSVer() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 42;i < 44; i++) {
            bTemp[i-42] = imageBytes[i];
        }        
        BPB_FSVer = byteArrayToInt(bTemp);
        System.out.println(BPB_FSVer); 
    }
    
    /**
     * Полечение номера первого кластера корневой директории
     * @throws UnsupportedEncodingException 
     */
    private void getBPB_RootClus() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 44;i < 48; i++) {
            bTemp[i-44] = imageBytes[i];
        }        
        BPB_RootClus = byteArrayToInt(bTemp);
        System.out.println(BPB_RootClus); 
    }
    
    /**
     * Получение строки "FAT32  " - не используется для определения FAT
     * @throws UnsupportedEncodingException 
     */
    private void getBPB_FilSysType() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[8];
        for (int i = 82;i < 90; i++) {
            bTemp[i-82] = imageBytes[i];
        }        
        BPB_FilSysType = new String(bTemp, "UTF-8");
        System.out.println(BPB_FilSysType); 
    }
    
    /**
     * Вычисление количества секторов занятой конревой директорией
     */
    private void calcRootDirSectors() {
        float rds = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec - 1f)) / BPB_BytsPerSec;
        RootDirSectors = Math.round(rds);
        System.out.println("RootDirSectors = " + RootDirSectors);
    }
    
    private void firstDataSector() {
        int FATSz;
        if (BPB_FATSz16 != 0) {
            FATSz = BPB_FATSz16;
        } else {
            FATSz = BPB_FATSz32;
        }
        FirstDataSector = BPB_ReservedSecCnt + (BPB_NumFATs * FATSz) + RootDirSectors;
        System.out.println("FirstDataSector = " + FirstDataSector);
    }
    
    @Override
    public void getFiles(String path) {
        
    }
    
    @Override
    public void getFile(String path) {
        
    }
}
