package asandrc.gmail.com.data;

import java.io.UnsupportedEncodingException;

public class FAT32Element {
    
    private String shortName;
    private String expansion;
    private Integer DIR_Attr;
    private Integer DIR_NTRes;
    private Integer DIR_CrtTimeTenth;
    private Integer DIR_CrtTime;
    private Integer DIR_CrtDate;
    private Integer DIR_LstAccDate;
    private Integer DIR_FstClusHI;
    private Integer DIR_WrtTime;
    private Integer DIR_WrtDate;
    private Integer DIR_FstClusLO;
    private Integer DIR_FileSize;
    
    private byte[] bytesOfFAT32Element;
    
    /**
     * Преобразует массив байтов в целое число
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
    
    public FAT32Element(byte[] bytes) throws UnsupportedEncodingException {
        if (bytes.length == 32) {
            bytesOfFAT32Element = bytes;
            init();
        } else {
            System.out.println("ERROR: FAT32 element can't be greater or lesser than 32 bytes!");
        }
    }
    
    private void init() throws UnsupportedEncodingException {
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
    }
    
    /**
     * Вычисление короткого имени файла
     * @throws UnsupportedEncodingException 
     */
    private void calcShortName() throws UnsupportedEncodingException {
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
    private void calcExpansion() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[3];
        for (int i = 8;i < 11; i++) {
            bTemp[i-8] = bytesOfFAT32Element[i];
        }        
        expansion = new String(bTemp, "UTF-8");
    }
    
    /**
     * Вычисление атрибутов файла
     */
    private void calcDIR_Attr() {
        byte[] bTemp = new byte[1];
        for (int i = 11;i < 12; i++) {
            bTemp[i-11] = bytesOfFAT32Element[i];
        }        
        DIR_Attr = byteArrayToInt(bTemp);
    }
    
    /**
     * Зарезервировано для Windows NT
     */
    private void calcDIR_NTRes() {
        byte[] bTemp = new byte[1];
        for (int i = 12;i < 13; i++) {
            bTemp[i-12] = bytesOfFAT32Element[i];
        }        
        DIR_NTRes = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление штампа милисекунд текущего времени
     */
    private void calcDIR_CrtTimeTenth() {
        byte[] bTemp = new byte[1];
        for (int i = 13;i < 14; i++) {
            bTemp[i-13] = bytesOfFAT32Element[i];
        }        
        DIR_CrtTimeTenth = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление времени создания файла
     */
    private void calcDIR_CrtTime() {
        byte[] bTemp = new byte[2];
        for (int i = 14;i < 16; i++) {
            bTemp[i-14] = bytesOfFAT32Element[i];
        }        
        DIR_CrtTime = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление даты создания файла
     */
    private void calcDIR_CrtDate() {
        byte[] bTemp = new byte[2];
        for (int i = 16;i < 18; i++) {
            bTemp[i-16] = bytesOfFAT32Element[i];
        }        
        DIR_CrtDate = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление даты последнего обращения файла
     */
    private void calcDIR_LstAccDate() {
        byte[] bTemp = new byte[2];
        for (int i = 18;i < 20; i++) {
            bTemp[i-18] = bytesOfFAT32Element[i];
        }        
        DIR_LstAccDate = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление старшего слова номера 1го кластера
     */
    private void calcDIR_FstClusHI() {
        byte[] bTemp = new byte[2];
        for (int i = 20;i < 22; i++) {
            bTemp[i-20] = bytesOfFAT32Element[i];
        }        
        DIR_FstClusHI = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление времени последней записи
     */
    private void calcDIR_WrtTime() {
        byte[] bTemp = new byte[2];
        for (int i = 22;i < 24; i++) {
            bTemp[i-22] = bytesOfFAT32Element[i];
        }        
        DIR_WrtTime = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление даты последней записи
     */
    private void calcDIR_WrtDate() {
        byte[] bTemp = new byte[2];
        for (int i = 24;i < 26; i++) {
            bTemp[i-24] = bytesOfFAT32Element[i];
        }        
        DIR_WrtDate = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление младшего слова номера первого кластера
     */
    private void calcDIR_FstClusLO() {
        byte[] bTemp = new byte[2];
        for (int i = 26;i < 28; i++) {
            bTemp[i-26] = bytesOfFAT32Element[i];
        }        
        DIR_FstClusLO = byteArrayToInt(bTemp);
    }
    
    /**
     * Вычисление размера файла в байтах
     */
    private void calcDIR_FileSize() {
        byte[] bTemp = new byte[4];
        for (int i = 28;i < 32; i++) {
            bTemp[i-28] = bytesOfFAT32Element[i];
        }        
        DIR_FileSize = byteArrayToInt(bTemp);
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

    public Integer getDIR_CrtTime() {
        return DIR_CrtTime;
    }

    public Integer getDIR_CrtDate() {
        return DIR_CrtDate;
    }

    public Integer getDIR_LstAccDate() {
        return DIR_LstAccDate;
    }

    public Integer getDIR_FstClusHI() {
        return DIR_FstClusHI;
    }

    public Integer getDIR_WrtTime() {
        return DIR_WrtTime;
    }

    public Integer getDIR_WrtDate() {
        return DIR_WrtDate;
    }

    public Integer getDIR_FstClusLO() {
        return DIR_FstClusLO;
    }

    public Integer getDIR_FileSize() {
        return DIR_FileSize;
    }
}
