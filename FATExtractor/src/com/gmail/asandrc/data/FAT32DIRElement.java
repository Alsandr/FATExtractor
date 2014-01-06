package com.gmail.asandrc.data;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Класс записи файла в файловой системе FAT32
 * @author Саша
 */
public class FAT32DIRElement {
    
    protected static final Integer DIR_ELEMENT_SIZE = 32;
    
    protected String shortName;
    protected String expansion;
    protected Integer DIR_Attr;
    protected Integer DIR_NTRes;
    protected Integer DIR_CrtTimeTenth;
    protected String DIR_CrtTime;
    protected String DIR_CrtDate;
    protected String DIR_LstAccDate;
    protected Integer DIR_FstClusHI;
    protected String DIR_WrtTime;
    protected String DIR_WrtDate;
    protected Integer DIR_FstClusLO;
    protected Integer DIR_FileSize;
    protected Integer fileSize;
    protected String fileSizeMod;
    protected Integer DIR_FstClusFULL;
    
    protected String crtTime;
    protected String crtDate;
    protected String wrtTime;
    protected String wrtDate;
    
    protected byte[] numbersOfClusters;
        
    protected byte[] bytesOfFAT32Element;
    
    /**
     * Преобразует массив байтов в целое число
     * @param bytes
     * @return result
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
     * Конструктор
     * @param bytes массив байтов элемента FAT32
     * @throws UnsupportedEncodingException 
     */
    public FAT32DIRElement(byte[] bytes) throws UnsupportedEncodingException {
        if (bytes.length == DIR_ELEMENT_SIZE) {
            bytesOfFAT32Element = bytes;
            init();
        } else {
            System.out.println("ERROR: FAT32DIRElement can't be greater or lesser than 32 bytes!");
        }
    }
    
    /**
     * Метод инициализации - вычисления основных параметров элемента FAT32
     * @throws UnsupportedEncodingException 
     */
    protected void init() throws UnsupportedEncodingException {
        calcShortName();
        calcExpansion();
        calcDIR_Attr();
        calcDIR_NTRes();
        calcDIR_CrtTimeTenth();
        calcDIR_CrtTime();
        calcDIR_CrtDate();
        calcDIR_LstAccDate();
        calcDIR_FstClusHI();
        calcDIR_WrtTime();
        calcDIR_WrtDate();
        calcDIR_FstClusLO();
        calcDIR_FileSize();
        calcDIR_FstClusFULL();
    }
    
    /**
     * Вычисление короткого имени файла
     * @throws UnsupportedEncodingException 
     */
    protected void calcShortName() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[8];
        for (int i = 0;i < 8; i++) {
            bTemp[i] = bytesOfFAT32Element[i];
        }        
        shortName = new String(bTemp, "UTF-8");
    }
    
    /**
     * Вычисление расширения файла
     * @throws UnsupportedEncodingException 
     */
    protected void calcExpansion() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[3];
        for (int i = 8;i < 11; i++) {
            bTemp[i-8] = bytesOfFAT32Element[i];
        }        
        expansion = new String(bTemp, "UTF-8");
    }
    
    /**
     * Вычисление атрибутов файла
     */
    protected void calcDIR_Attr() {
        byte[] bTemp = new byte[1];
        for (int i = 11;i < 12; i++) {
            bTemp[i-11] = bytesOfFAT32Element[i];
        }        
        DIR_Attr = byteArrayToInt(bTemp);
    }
    
    /**
     * Зарезервировано для Windows NT
     */
    protected void calcDIR_NTRes() {
        byte[] bTemp = new byte[1];
        for (int i = 12;i < 13; i++) {
            bTemp[i-12] = bytesOfFAT32Element[i];
        }        
        DIR_NTRes = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление штампа милисекунд текущего времени
     */
    protected void calcDIR_CrtTimeTenth() {
        byte[] bTemp = new byte[1];
        for (int i = 13;i < 14; i++) {
            bTemp[i-13] = bytesOfFAT32Element[i];
        }        
        DIR_CrtTimeTenth = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление времени создания файла
     */
    protected void calcDIR_CrtTime() {
        byte[] bTemp = new byte[2];
        for (int i = 14;i < 16; i++) {
            bTemp[i-14] = bytesOfFAT32Element[i];
        }
        Integer temp = byteArrayToInt(bTemp);
        
        DIR_CrtTime = ((temp & 0b111110000000000) >> 10) + ":" + ((temp & 0b1111110000) >> 5) + "." + (temp & 0b1111) * 2;
    }
    
    /**
     * Вычисление даты создания файла
     */
    protected void calcDIR_CrtDate() {
        byte[] bTemp = new byte[2];
        for (int i = 16;i < 18; i++) {
            bTemp[i-16] = bytesOfFAT32Element[i];
        }
        
        Integer temp = byteArrayToInt(bTemp);
        
        DIR_CrtDate = (temp & 0b11111) + "." + ((temp & 0b111100000) >> 5) + "." + (((temp & 0b1111111000000000) >> 9) + 1980);
    }
    
    /**
     * Вычисление даты последнего обращения файла
     */
    protected void calcDIR_LstAccDate() {
        byte[] bTemp = new byte[2];
        for (int i = 18;i < 20; i++) {
            bTemp[i-18] = bytesOfFAT32Element[i];
        }     
        Integer temp = byteArrayToInt(bTemp);
        DIR_LstAccDate = (temp & 0b11111) + "." + ((temp & 0b111100000) >> 5) + "." + (((temp & 0b1111111000000000) >> 9) + 1980);
    }
    
    /**
     * Вычисление старшего слова номера 1го кластера
     */
    protected void calcDIR_FstClusHI() {
        byte[] bTemp = new byte[2];
        for (int i = 20;i < 22; i++) {
            bTemp[i-20] = bytesOfFAT32Element[i];
        }
        DIR_FstClusHI = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление времени последней записи
     */
    protected void calcDIR_WrtTime() {
        byte[] bTemp = new byte[2];
        for (int i = 22;i < 24; i++) {
            bTemp[i-22] = bytesOfFAT32Element[i];
        }
        Integer temp = byteArrayToInt(bTemp);
        
        DIR_WrtTime = ((temp & 0b111110000000000) >> 10) + ":" + ((temp & 0b1111110000) >> 4) + "." + (temp & 0b1111) * 2;
    }
    
    /**
     * Вычисление даты последней записи
     */
    protected void calcDIR_WrtDate() {
        byte[] bTemp = new byte[2];
        for (int i = 24;i < 26; i++) {
            bTemp[i-24] = bytesOfFAT32Element[i];
        }
        Integer temp = byteArrayToInt(bTemp);
        DIR_WrtDate =  (temp & 0b11111) + "." + ((temp & 0b111100000) >> 5) + "." + (((temp & 0b1111111000000000) >> 9) + 1980);        
    }
    
    /**
     * Вычисление младшего слова номера первого кластера
     */
    protected void calcDIR_FstClusLO() {
        byte[] bTemp = new byte[2];
        for (int i = 26;i < 28; i++) {
            bTemp[i-26] = bytesOfFAT32Element[i];
        }        
        DIR_FstClusLO = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление размера файла в байтах
     */
    protected void calcDIR_FileSize() {
        byte[] bTemp = new byte[4];
        for (int i = 28;i < 32; i++) {
            bTemp[i-28] = bytesOfFAT32Element[i];
        }        
        DIR_FileSize = byteArrayToInt(bTemp);
        if (DIR_FileSize >= 1024) {
            fileSize = DIR_FileSize / 1024;
            fileSizeMod = "Кб";
        } else {
           fileSize = DIR_FileSize;
           fileSizeMod = "байт"; 
        }
    }
    
    /**
     * Вычисление двойного слова номера 1го кластера
     */
    protected void calcDIR_FstClusFULL() {
        byte[] bTemp = new byte[4];
        for (int i = 26;i < 28; i++) {
            bTemp[i-26] = bytesOfFAT32Element[i];
        }  
        for (int i = 20;i < 22; i++) {
            bTemp[i-18] = bytesOfFAT32Element[i];
        }
        DIR_FstClusFULL = byteArrayToInt(bTemp);
    }

    public String getShortName() {
        return shortName;
    }

    public String getExpansion() {
        return expansion;
    }

    public Integer getDIR_Attr() {
        return DIR_Attr;
    }

    public Integer getDIR_NTRes() {
        return DIR_NTRes;
    }

    public Integer getDIR_CrtTimeTenth() {
        return DIR_CrtTimeTenth;
    }

    public String getDIR_CrtTime() {
        return DIR_CrtTime;
    }

    public String getDIR_CrtDate() {
        return DIR_CrtDate;
    }

    public String getDIR_LstAccDate() {
        return DIR_LstAccDate;
    }

    public Integer getDIR_FstClusHI() {
        return DIR_FstClusHI;
    }

    public String getDIR_WrtTime() {
        return DIR_WrtTime;
    }

    public String getDIR_WrtDate() {
        return DIR_WrtDate;
    }

    public Integer getDIR_FstClusLO() {
        return DIR_FstClusLO;
    }

    public Integer getDIR_FileSize() {
        return DIR_FileSize;
    }

    public Integer getDIR_FstClusFULL() {
        return DIR_FstClusFULL;
    }
    
    @Override
    public String toString() {
        return shortName.trim() + "." + expansion + "   " + fileSize + " "+ fileSizeMod + "   " + DIR_CrtDate + "   " + DIR_CrtTime;
    }
}
