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
    private Integer BPB_TotSec16;
    private Integer BPB_TotSec32;
    private Integer BPB_FATSz16;
    private Integer BPB_FATSz32;
    private Integer FATSz;
    private Integer BPB_FSVer;
    private Integer BPB_RootClus;
    private String BPB_FilSysType;
    private Integer BPB_RootEntCnt;
    private Integer firstDataSector;
    private Integer dataSecCnt;
    private Integer countOfClusters;
    private String typeOfFileSystem;
    
    private Integer thisFATSecNum;
    private Integer thisFATEntOffset;
    
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
        getBPB_TotSec16();
        getBPB_TotSec32();
        getBPB_FATSz32();
        calcFATSz();
        getBPB_FSVer();
        getBPB_RootClus();
        getBPB_FilSysType();
        calcRootDirSectors();
        firstDataSector();
        calcDataSecCnt();
        calcCountOfClusters();
        getTypeOfFileSytmes();        
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
    private String getBS_OEMNAME() throws IOException {
        byte[] bTemp = new byte[8];
        for (int i = 3;i < 11; i++) {
            bTemp[i-3] = imageBytes[i];
        }        
        BS_OEMNAme = new String(bTemp, "UTF-8");
        System.out.println(BS_OEMNAme);
        return BS_OEMNAme;
    }
    
    /**
     * Получение количества байтов в секторе
     * @throws UnsupportedEncodingException 
     */
    private Integer getBPB_BytsPerSec() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 11;i < 13; i++) {
            bTemp[i-11] = imageBytes[i];
        }        
        BPB_BytsPerSec = byteArrayToInt(bTemp);
        System.out.println(BPB_BytsPerSec);
        return BPB_BytsPerSec;
    }
    
    /**
     * Получение количества секторов в кластере
     * @throws UnsupportedEncodingException 
     */
    private Integer getBPB_SecPerClus() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[1];
        for (int i = 13;i < 14; i++) {
            bTemp[i-13] = imageBytes[i];
        }        
        BPB_SecPerClus = byteArrayToInt(bTemp);
        System.out.println(BPB_SecPerClus);
        return BPB_SecPerClus;
    }
    
    /**
     * Получение количества секторов в Reserved region
     * @throws UnsupportedEncodingException 
     */
    private Integer getBPB_ReservedSecCnt() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 14;i < 16; i++) {
            bTemp[i-14] = imageBytes[i];
        }        
        BPB_ReservedSecCnt = byteArrayToInt(bTemp);
        System.out.println(BPB_ReservedSecCnt);
        return BPB_ReservedSecCnt;
    }
    
    /**
     * Получение количесвта FAT таблиц на диске
     * @throws UnsupportedEncodingException 
     */
    private Integer getBPB_NumFATs() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[1];
        for (int i = 16;i < 17; i++) {
            bTemp[i-16] = imageBytes[i];
        }        
        BPB_NumFATs = byteArrayToInt(bTemp);
        System.out.println(BPB_NumFATs);
        return BPB_NumFATs;
    }
    
    
    private Integer getBPB_RootEntCnt() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 17;i < 19; i++) {
            bTemp[i-17] = imageBytes[i];
        }        
        BPB_RootEntCnt = byteArrayToInt(bTemp);
        System.out.println(BPB_RootEntCnt);
        return BPB_RootEntCnt;
    }
    
    /**
     * Получение количества секторов одной FAT (для FAT12/FAT16)
     * @throws UnsupportedEncodingException 
     */
    private Integer getBPB_FATSz16() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 22;i < 24; i++) {
            bTemp[i-22] = imageBytes[i];
        }        
        BPB_FATSz16 = byteArrayToInt(bTemp);
        System.out.println(BPB_FATSz16);
        return BPB_FATSz16;
    }
    
    private Integer getBPB_TotSec16() {
        byte[] bTemp = new byte[2];
        for (int i = 19;i < 21; i++) {
            bTemp[i-19] = imageBytes[i];
        }        
        BPB_TotSec16 = byteArrayToInt(bTemp);
        System.out.println(BPB_TotSec16);
        return BPB_TotSec16;
    }
    
    /**
     * Получение общего количества секторов на диске (для FAT32)
     * @throws UnsupportedEncodingException 
     */
    private Integer getBPB_TotSec32() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 32;i < 36; i++) {
            bTemp[i-32] = imageBytes[i];
        }        
        BPB_TotSec32 = byteArrayToInt(bTemp);
        System.out.println(BPB_TotSec32);
        return BPB_TotSec32;
    }
    
    /**
     * Получение количества секторов одной FAT (для FAT32)
     * @throws UnsupportedEncodingException 
     */
    private Integer getBPB_FATSz32() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 36;i < 40; i++) {
            bTemp[i-36] = imageBytes[i];
        }        
        BPB_FATSz32 = byteArrayToInt(bTemp);
        System.out.println(BPB_FATSz32);
        return BPB_FATSz32;
    }
    
    /**
     * Получение венрсии FAT32 (старший байт - номер версии, 
     * младший - номер промежуточной версии)
     * @throws UnsupportedEncodingException 
     */
    private Integer getBPB_FSVer() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 42;i < 44; i++) {
            bTemp[i-42] = imageBytes[i];
        }        
        BPB_FSVer = byteArrayToInt(bTemp);
        System.out.println(BPB_FSVer);
        return BPB_FSVer;
    }
    
    /**
     * Полечение номера первого кластера корневой директории
     * @throws UnsupportedEncodingException 
     */
    private Integer getBPB_RootClus() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 44;i < 48; i++) {
            bTemp[i-44] = imageBytes[i];
        }        
        BPB_RootClus = byteArrayToInt(bTemp);
        System.out.println(BPB_RootClus);
        return BPB_RootClus;
    }
    
    /**
     * Получение строки "FAT32  " - не используется для определения FAT
     * @throws UnsupportedEncodingException 
     */
    private String getBPB_FilSysType() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[8];
        for (int i = 82;i < 90; i++) {
            bTemp[i-82] = imageBytes[i];
        }        
        BPB_FilSysType = new String(bTemp, "UTF-8");
        System.out.println(BPB_FilSysType);
        return BPB_FilSysType;
    }
    
    /**
     * Вычисление количества секторов занятой конревой директорией
     */
    private Integer calcRootDirSectors() {
        float rds = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec - 1f)) / BPB_BytsPerSec;
        RootDirSectors = Math.round(rds);
        System.out.println("RootDirSectors = " + RootDirSectors);
        return RootDirSectors;
    }
    
    private void calcFATSz() {        
        if (BPB_FATSz16 != 0) {
            FATSz = BPB_FATSz16;
        } else {
            FATSz = BPB_FATSz32;
        }
    }
                
    /**
     * Начало региона данных
     */
    private Integer firstDataSector() {        
        firstDataSector = BPB_ReservedSecCnt + (BPB_NumFATs * FATSz) + RootDirSectors;
        System.out.println("FirstDataSector = " + firstDataSector);
        return firstDataSector;
    }
    
    /**
     * Вычисление количества секторов в регионе данных
     */
    private Integer calcDataSecCnt() {        
        int TotSec;
        if (BPB_TotSec16 != 0) {
            TotSec = BPB_TotSec16;
        } else {
            TotSec = BPB_TotSec32;
        }
        dataSecCnt = TotSec - (BPB_ReservedSecCnt - (BPB_NumFATs * FATSz) + RootDirSectors);
        System.out.println("DataSecCnt = " + dataSecCnt);
        return dataSecCnt;
    }
    
    /**
     * Вычисление количества кластеров
     */
    private Integer calcCountOfClusters() {
        countOfClusters = dataSecCnt / BPB_SecPerClus;
        System.out.println("CountOfClusters = " + countOfClusters);
        return countOfClusters;
    }
    
    /**
     * Определение типа файловой системы
     */
    private String getTypeOfFileSytmes() {
        if (countOfClusters < 4085) {
            typeOfFileSystem = "FAT12";
        } else if (countOfClusters < 65525) {
            typeOfFileSystem = "FAT16";
        } else {
            typeOfFileSystem = "FAT32";
        }
        System.out.println(typeOfFileSystem);
        return typeOfFileSystem;
    }
    
    /**
     * Вычисление номера 1-о сектора кластера N
     * @param N номер кластера
     * @return номер 1-го сектора кластера N
     */
    public int calcFirstSecOfCluster(int N) {
        return ((N - 1) * BPB_SecPerClus) + firstDataSector;
    }
    
    /**
     * Вычисление входной точки в таблице FAT
     * @param N номер кластера
     */
    public Integer calcThisFATEntOffset(int N) {
        int FATOffset = 0;
        if (typeOfFileSystem.equalsIgnoreCase("FAT16")) {
            FATOffset = N * 2;
        } else if (typeOfFileSystem.equalsIgnoreCase("FAT32")) {
            FATOffset = N * 4;
        }
        thisFATSecNum = BPB_ReservedSecCnt + (FATOffset / BPB_BytsPerSec);
        thisFATEntOffset = FATOffset % BPB_BytsPerSec;
        System.out.println("thisFATSecNum = " + thisFATSecNum);
        System.out.println("thisFATEntOffset = " + thisFATEntOffset);
        return thisFATEntOffset;
    }
    
    @Override
    public void getFiles(String path) {
        
    }
    
    @Override
    public void getFile(String path) {
        
    }

    
}
