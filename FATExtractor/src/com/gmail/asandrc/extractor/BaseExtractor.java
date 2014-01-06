package com.gmail.asandrc.extractor;

import com.gmail.asandrc.data.FAT32DIRElement;
import com.gmail.asandrc.data.FAT32Directory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Базовый класс FATExtractor для реализации FAT32Extractor
 * и в будущем для FAT16Extractor
 * @author Саша
 */
public class BaseExtractor {
    
    //атрибуты файлов
    protected static final Integer ATTR_READ_ONLY = 0x01;
    protected static final Integer ATTR_HIDDEN = 0x02;
    protected static final Integer ATTR_SYSTEM = 0x04;
    protected static final Integer ATTR_VOLUME_ID = 0x08;
    protected static final Integer ATTR_DIRECTORY = 0x10;
    protected static final Integer ATTR_ARCHIVE = 0x20;
    protected static final Integer ATTR_LONG_NAME = ATTR_READ_ONLY | ATTR_HIDDEN 
            | ATTR_SYSTEM | ATTR_VOLUME_ID;
    protected static final Integer ATTR_LONG_NAME_MASK = ATTR_READ_ONLY 
            | ATTR_HIDDEN | ATTR_SYSTEM | ATTR_VOLUME_ID | ATTR_DIRECTORY | ATTR_ARCHIVE;
    
    protected String BS_OEMNAme;
    protected Integer BPB_BytsPerSec;
    protected Integer BPB_SecPerClus;
    protected Integer BPB_ReservedSecCnt;
    protected Integer BPB_NumFATs;
    protected Integer BPB_TotSec16;
    protected Integer BPB_TotSec32;
    protected Integer BPB_FATSz16;
    protected Integer BPB_FATSz32;
    protected Integer FATSz;
    protected Integer BPB_FSVer;
    protected Integer BPB_RootClus;
    protected String BPB_FilSysType;
    protected Integer BPB_RootEntCnt;
    protected Integer firstDataSector;
    protected Integer dataSecCnt;
    protected Integer countOfClusters;
    
    protected String typeOfFileSystem;
    
    protected byte[] imageBytes;
    
    protected File image;
    protected FileInputStream fs;
    
    protected Integer thisFATSecNum;
    protected Integer thisFATEntOffset;
    protected Integer endOfClusterchain;
    
    protected Integer RootDirSectors;
    
    protected FAT32Directory rootElement;
    
    /**
     * Открытие файла (образа) с системой FAT
     * (получение массива байтов файла)
     * @param FATfile фалй (образ)
     */
    public boolean openFAT(File FATfile) {
        image = FATfile;
        if (image.exists()) {
            try {
                fs = new FileInputStream(image);
                try {
                    init();
                } catch (IOException ex) {
                    Logger.getLogger(FAT32Extractor.class.getName()).log(Level.SEVERE, null, ex);
                }
                return true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FAT32Extractor.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        } else {
            System.out.println("File doesn't exsist");
            return false;
        }
    }
    
    /**
     * Метод инициализации FAT32Extractor-а
     * получение байтов файла (образа) и информации о файловой системе
     * @throws IOException 
     */
    protected void init() throws IOException {
        getImageBytes();
        calsBS_OEMNAME();
        calcBPB_BytsPerSec();
        calcBPB_SecPerClus();
        calcBPB_ReservedSecCnt();
        calcBPB_NumFATs();
        calcBPB_FATSz16();
        calcBPB_RootEntCnt();
        calcBPB_TotSec16();
        calcBPB_TotSec32();
        calcBPB_FATSz32();
        calcFATSz();
        calcBPB_FSVer();
        calcBPB_RootClus();
        calcBPB_FilSysType();
        calcRootDirSectors();
        firstDataSector();
        calcDataSecCnt();
        calcCountOfClusters();
    }
    
    /**
     * Метод извлечения файла
     * @param f32DIRElement f32DIRElement файла, который необходимо извлечь
     * @return массив байтов файла
     */
    public byte[] extractFile(FAT32DIRElement f32DIRElement) {
        return new byte[32];
    }
    
    /**
     * Получить байты файла (образа)
     * @throws IOException 
     */    
    protected void getImageBytes() throws IOException {
        imageBytes = new byte[102400000];
        fs.read(imageBytes, 0, 102400000);
    }
    
    /**
     * Преобразует массив байтов в целое число
     * @param bytes
     * @return 
     */
    protected int byteArrayToInt(byte[] bytes) {
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
    protected String calsBS_OEMNAME() throws IOException {
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
    protected Integer calcBPB_BytsPerSec() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 11;i < 13; i++) {
            bTemp[i-11] = imageBytes[i];
        }
        BPB_BytsPerSec = byteArrayToInt(bTemp);
        System.out.println("BPB_BytsPerSec = " + BPB_BytsPerSec);
        return BPB_BytsPerSec;
    }
    
    /**
     * Получение количества секторов в кластере
     * @throws UnsupportedEncodingException 
     */
    protected Integer calcBPB_SecPerClus() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[1];
        for (int i = 13;i < 14; i++) {
            bTemp[i-13] = imageBytes[i];
        }
        BPB_SecPerClus = byteArrayToInt(bTemp);
        System.out.println("BPB_SecPerClus = " + BPB_SecPerClus);
        return BPB_SecPerClus;
    }
    
    /**
     * Получение количества секторов в Reserved region
     * @throws UnsupportedEncodingException 
     */
    protected Integer calcBPB_ReservedSecCnt() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 14;i < 16; i++) {
            bTemp[i-14] = imageBytes[i];
        }
        BPB_ReservedSecCnt = byteArrayToInt(bTemp);
        System.out.println("Количество секторов в Reserved region = " + BPB_ReservedSecCnt);
        return BPB_ReservedSecCnt;
    }
    
    /**
     * Получение количесвта FAT таблиц на диске
     * @throws UnsupportedEncodingException 
     */
    protected Integer calcBPB_NumFATs() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[1];
        for (int i = 16;i < 17; i++) {
            bTemp[i-16] = imageBytes[i];
        }
        BPB_NumFATs = byteArrayToInt(bTemp);
        System.out.println("BPB_NumFATs = " + BPB_NumFATs);
        return BPB_NumFATs;
    }
    
    
    protected Integer calcBPB_RootEntCnt() throws UnsupportedEncodingException {
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
    protected Integer calcBPB_FATSz16() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 22;i < 24; i++) {
            bTemp[i-22] = imageBytes[i];
        }
        BPB_FATSz16 = byteArrayToInt(bTemp);
        System.out.println(BPB_FATSz16);
        return BPB_FATSz16;
    }
    
    protected Integer calcBPB_TotSec16() {
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
    protected Integer calcBPB_TotSec32() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 32;i < 36; i++) {
            bTemp[i-32] = imageBytes[i];
        }
        BPB_TotSec32 = byteArrayToInt(bTemp);
        System.out.println("Общее количество секторов = " + BPB_TotSec32);
        return BPB_TotSec32;
    }
    
    /**
     * Получение количества секторов одной FAT (для FAT32)
     * @throws UnsupportedEncodingException 
     */
    protected Integer calcBPB_FATSz32() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 36;i < 40; i++) {
            bTemp[i-36] = imageBytes[i];
        }
        BPB_FATSz32 = byteArrayToInt(bTemp);
        System.out.println("FATSz32 (количество секторов) = " + BPB_FATSz32);
        return BPB_FATSz32;
    }
    
    /**
     * Получение версии FAT32 (старший байт - номер версии, 
     * младший - номер промежуточной версии)
     * @throws UnsupportedEncodingException 
     */
    protected Integer calcBPB_FSVer() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 42;i < 44; i++) {
            bTemp[i-42] = imageBytes[i];
        }
        BPB_FSVer = byteArrayToInt(bTemp);
        System.out.println("Номер версии FAT32 = " + BPB_FSVer);
        return BPB_FSVer;
    }
    
    /**
     * Получение номера первого кластера корневой директории
     * @throws UnsupportedEncodingException 
     */
    protected Integer calcBPB_RootClus() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[4];
        for (int i = 44;i < 48; i++) {
            bTemp[i-44] = imageBytes[i];
        }
        BPB_RootClus = byteArrayToInt(bTemp);
        System.out.println("Первый кластер корневой директории = " + BPB_RootClus);
        return BPB_RootClus;
    }
    
    /**
     * Получение строки "FAT32  " - не используется для определения FAT
     * @throws UnsupportedEncodingException 
     */
    protected String calcBPB_FilSysType() throws UnsupportedEncodingException {
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
    protected Integer calcRootDirSectors() {
        float rds = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec - 1)) / BPB_BytsPerSec;
        RootDirSectors = Math.round(rds);
        System.out.println("RootDirSectors = " + RootDirSectors);
        return RootDirSectors;
    }
    
    protected void calcFATSz() {        
        if (BPB_FATSz16 != 0) {
            FATSz = BPB_FATSz16;
        } else {
            FATSz = BPB_FATSz32;
        }
    }
                
    /**
     * Начало региона данных
     */
    protected Integer firstDataSector() {        
        firstDataSector = BPB_ReservedSecCnt + (BPB_NumFATs * FATSz) + RootDirSectors;
        System.out.println("FirstDataSector = " + firstDataSector);
        return firstDataSector;
    }
    
    /**
     * Вычисление количества секторов в регионе данных
     */
    protected Integer calcDataSecCnt() {        
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
    protected Integer calcCountOfClusters() {
        countOfClusters = dataSecCnt / BPB_SecPerClus;
        System.out.println("CountOfClusters = " + countOfClusters);
        return countOfClusters;
    }
    
    public FAT32Directory getRootElement() {        // топор! сделать для FAT32Diredtory базовый класс
        return rootElement;
    }
    
    public String getBS_OEMNAme() {
        return BS_OEMNAme;
    }

    public Integer getBPB_BytsPerSec() {
        return BPB_BytsPerSec;
    }

    public Integer getBPB_SecPerClus() {
        return BPB_SecPerClus;
    }

    public Integer getBPB_ReservedSecCnt() {
        return BPB_ReservedSecCnt;
    }

    public Integer getBPB_NumFATs() {
        return BPB_NumFATs;
    }

    public Integer getBPB_TotSec16() {
        return BPB_TotSec16;
    }

    public Integer getBPB_TotSec32() {
        return BPB_TotSec32;
    }

    public Integer getBPB_FATSz16() {
        return BPB_FATSz16;
    }

    public Integer getBPB_FATSz32() {
        return BPB_FATSz32;
    }

    public Integer getFATSz() {
        return FATSz;
    }

    public Integer getBPB_FSVer() {
        return BPB_FSVer;
    }

    public Integer getBPB_RootClus() {
        return BPB_RootClus;
    }

    public String getBPB_FilSysType() {
        return BPB_FilSysType;
    }

    public Integer getBPB_RootEntCnt() {
        return BPB_RootEntCnt;
    }

    public Integer getFirstDataSector() {
        return firstDataSector;
    }

    public Integer getDataSecCnt() {
        return dataSecCnt;
    }

    public Integer getCountOfClusters() {
        return countOfClusters;
    }

    public String getTypeOfFileSystem() {
        return typeOfFileSystem;
    }

    public Integer getThisFATSecNum() {
        return thisFATSecNum;
    }

    public Integer getThisFATEntOffset() {
        return thisFATEntOffset;
    }

    public Integer getRootDirSectors() {
        return RootDirSectors;
    }

    public Integer getEndOfClusterchain() {
        return endOfClusterchain;
    }

    public static Integer getATTR_READ_ONLY() {
        return ATTR_READ_ONLY;
    }

    public static Integer getATTR_HIDDEN() {
        return ATTR_HIDDEN;
    }

    public static Integer getATTR_SYSTEM() {
        return ATTR_SYSTEM;
    }

    public static Integer getATTR_VOLUME_ID() {
        return ATTR_VOLUME_ID;
    }

    public static Integer getATTR_DIRECTORY() {
        return ATTR_DIRECTORY;
    }

    public static Integer getATTR_ARCHIVE() {
        return ATTR_ARCHIVE;
    }

    public static Integer getATTR_LONG_NAME() {
        return ATTR_LONG_NAME;
    }

    public static Integer getATTR_LONG_NAME_MASK() {
        return ATTR_LONG_NAME_MASK;
    }
}
