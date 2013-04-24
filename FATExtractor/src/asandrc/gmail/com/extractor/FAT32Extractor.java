package asandrc.gmail.com.extractor;

import asandrc.gmail.com.data.FAT32DIRElement;
import asandrc.gmail.com.data.FAT32Directory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Саша
 */
public class FAT32Extractor extends BaseExtractor {
    
    //атрибуты файлов
    private static final Integer ATTR_READ_ONLY = 0x01;
    private static final Integer ATTR_HIDDEN = 0x02;
    private static final Integer ATTR_SYSTEM = 0x04;
    private static final Integer ATTR_VOLUME_ID = 0x08;
    private static final Integer ATTR_DIRECTORY = 0x10;
    private static final Integer ATTR_ARCHIVE = 0x20;
    private static final Integer ATTR_LONG_NAME = ATTR_READ_ONLY | ATTR_HIDDEN 
            | ATTR_SYSTEM | ATTR_VOLUME_ID;
    private static final Integer ATTR_LONG_NAME_MASK = ATTR_READ_ONLY 
            | ATTR_HIDDEN | ATTR_SYSTEM | ATTR_VOLUME_ID | ATTR_DIRECTORY | ATTR_ARCHIVE;
    
    private static final Integer DIR_ELEMENT_SIZE = 32;
    
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
    private Integer endOfClusterchain;
    
    private Integer RootDirSectors;
    
    private File image;
    private FileInputStream fs;
    
    private byte[] bytesOfF32DIRElement;
    
    private FAT32Directory rootElement;
    
    /**
     * Метод открытия файла (образа) файловой системы
     * @param path путь к файлу (образу)
     */
    @Override
    public void openFAT(File FATfile) {
        image = FATfile;
        if (image.exists()) {
            try {
                fs = new FileInputStream(image);
                try {
                    init();
                } catch (IOException ex) {
                    Logger.getLogger(FAT32Extractor.class.getName()).log(Level.SEVERE, null, ex);
                }
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
    private void init() throws IOException {
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
        calcTypeOfFileSytmes();
        searchFAT32Element();
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
    
    /**
     * Получение строки имени системы, какой был отформатирована файловая система
     * @throws IOException 
     */
    private String calsBS_OEMNAME() throws IOException {
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
    private Integer calcBPB_BytsPerSec() throws UnsupportedEncodingException {
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
    private Integer calcBPB_SecPerClus() throws UnsupportedEncodingException {
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
    private Integer calcBPB_ReservedSecCnt() throws UnsupportedEncodingException {
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
    private Integer calcBPB_NumFATs() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[1];
        for (int i = 16;i < 17; i++) {
            bTemp[i-16] = imageBytes[i];
        }
        BPB_NumFATs = byteArrayToInt(bTemp);
        System.out.println("BPB_NumFATs = " + BPB_NumFATs);
        return BPB_NumFATs;
    }
    
    
    private Integer calcBPB_RootEntCnt() throws UnsupportedEncodingException {
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
    private Integer calcBPB_FATSz16() throws UnsupportedEncodingException {
        byte[] bTemp = new byte[2];
        for (int i = 22;i < 24; i++) {
            bTemp[i-22] = imageBytes[i];
        }
        BPB_FATSz16 = byteArrayToInt(bTemp);
        System.out.println(BPB_FATSz16);
        return BPB_FATSz16;
    }
    
    private Integer calcBPB_TotSec16() {
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
    private Integer calcBPB_TotSec32() throws UnsupportedEncodingException {
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
    private Integer calcBPB_FATSz32() throws UnsupportedEncodingException {
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
    private Integer calcBPB_FSVer() throws UnsupportedEncodingException {
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
    private Integer calcBPB_RootClus() throws UnsupportedEncodingException {
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
    private String calcBPB_FilSysType() throws UnsupportedEncodingException {
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
        float rds = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec - 1)) / BPB_BytsPerSec;
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
    private String calcTypeOfFileSytmes() {
        if (countOfClusters < 4085) {
            typeOfFileSystem = "FAT12";
            endOfClusterchain = 0x0FF8;
        } else if (countOfClusters < 65525) {
            typeOfFileSystem = "FAT16";
            endOfClusterchain = 0xFFF8;
        } else {
            typeOfFileSystem = "FAT32";
            endOfClusterchain = 0x0FFFFFF8;
        }
        System.out.println("Тип файловой системы:" + typeOfFileSystem);
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
    public void calcThisFATEntOffset(int N) {
        int FATOffset = 0;
        if (typeOfFileSystem.equalsIgnoreCase("FAT16")) {
            FATOffset = N * 2;
        } else if (typeOfFileSystem.equalsIgnoreCase("FAT32")) {
            FATOffset = N * 4;
        }
        thisFATSecNum = BPB_ReservedSecCnt + (FATOffset / BPB_BytsPerSec);
        thisFATEntOffset = FATOffset % BPB_BytsPerSec;
    }
    
    public void searchFAT32Element() throws UnsupportedEncodingException {
        //получение корневого элемента FAT32
        byte[] rootBytes = new byte[32];
        for (int i = BPB_RootClus * BPB_BytsPerSec; i < (BPB_RootClus * BPB_BytsPerSec + 32); i++) {                
            rootBytes[i - BPB_RootClus * BPB_BytsPerSec] = imageBytes[i];
        }
        rootElement = new FAT32Directory(rootBytes);
        //явная установка имени (для удобства отображения)
        rootElement.setShortName("\\");
        
        // первый байт области данных
        int firstDataByte = firstDataSector * BPB_BytsPerSec;
        System.out.println("firstDataByte = " + firstDataByte);
        
        buildHierarchy(rootElement, firstDataByte);
    }
    
    /**
     * Метод построения иерархии директорий и вложеных в них файлов
     * @param rootElement передаваемый кореневой элемент
     * @param byteOffset байтовое смещение по записям каталога
     * @throws UnsupportedEncodingException 
     */
    private void buildHierarchy(FAT32Directory rootElement, int byteOffset) throws UnsupportedEncodingException {
        // для перехода по 32-байтовым записам директории
        int c = 0;
        int i = byteOffset + (DIR_ELEMENT_SIZE * c);
        // проверка на пустую запись - поиск элементов
        while ((imageBytes[i] != 0) && (imageBytes[i] != 0xE5)) {            
            byte[] bTemp = new byte[32];
            for (int j = i; j < (i + 32); j++) {        // загрузка в промежуточный массив 32-байтный элемент директории
                bTemp[j-i] = imageBytes[j];
            }
            FAT32DIRElement f32El = new FAT32DIRElement(bTemp);
            if ((f32El.getDIR_Attr() & ATTR_DIRECTORY) == ATTR_DIRECTORY && (f32El.getDIR_Attr()        //проверка на директорию, длинное имя, 
                    & ATTR_LONG_NAME) != ATTR_LONG_NAME && f32El.getDIR_NTRes() == 0                    //".", ".." - так делать не хорошо
                    && f32El.getShortName().compareTo(".       ") != 0
                    && f32El.getShortName().compareTo("..      ") != 0) {
                FAT32Directory f32DIR = new FAT32Directory(bTemp);
                int offset = ((firstDataSector + f32DIR.getDIR_FstClusFULL()) * BPB_BytsPerSec) - (2 * BPB_BytsPerSec);     //вычисляем смещение в области данных для
                buildHierarchy(f32DIR, offset);                                                                             // кластера вложенной директории и запуск метода для нее
                rootElement.getChildElements().add(f32DIR);
                rootElement.getChildDirectories().add(f32DIR);
            } else if ((f32El.getDIR_Attr() & ATTR_DIRECTORY) != ATTR_DIRECTORY && (f32El.getDIR_Attr()     // проверка на файл
                    & ATTR_LONG_NAME) != ATTR_LONG_NAME && f32El.getDIR_NTRes() == 0) {
                rootElement.getChildElements().add(f32El);
            }
            // инкрементировав счетчик, пересчитуется указатель на следующюю запись директории
            c++;
            i = byteOffset + (DIR_ELEMENT_SIZE * c); 
        }
    }
        
    @Override
    public byte[] extractFile(FAT32DIRElement f32DIRElement) {
        int countFileClusters = 0;
        if (f32DIRElement.getDIR_FileSize() < BPB_BytsPerSec) {
            countFileClusters = 1;
        } else {
            countFileClusters = f32DIRElement.getDIR_FileSize() / BPB_BytsPerSec + 1;
        }
        int c = 0;
        bytesOfF32DIRElement = new byte[BPB_BytsPerSec * countFileClusters];
        System.out.println("======================================");
        System.out.println("Size of byteArr = " + bytesOfF32DIRElement.length);
        checkEOF(f32DIRElement.getDIR_FstClusFULL(), bytesOfF32DIRElement, c);        
        
        return bytesOfF32DIRElement;
    }
    
    private void checkEOF(int N, byte[] bytesOfF32DIRElement, int countCl) {        
        byte[] bTemp = readCluster(N);
        
        System.out.println("countCL = " + countCl);
        for (int i = 0; i < BPB_BytsPerSec; i++) {
            bytesOfF32DIRElement[i + countCl * BPB_BytsPerSec] = bTemp[i];
        }
        calcThisFATEntOffset(N);
        byte[] bCheckEOF = new byte[4];
        
        int beginOffset = thisFATSecNum * BPB_BytsPerSec + thisFATEntOffset;
        int endOffset = thisFATSecNum * BPB_BytsPerSec + thisFATEntOffset + 4;
        System.out.println("FAT offset = " + beginOffset);
        for (int j = beginOffset; j < endOffset; j++) {
            System.out.println(j - beginOffset);
            bCheckEOF[j - beginOffset] = imageBytes[j];
        }
        int resultEOF = byteArrayToInt(bCheckEOF);
        System.out.println("Cluster value = " + resultEOF);
        if (resultEOF >= 0x0FFFFFFF) {
            
        } else {
            countCl++;
            checkEOF(resultEOF, bytesOfF32DIRElement, countCl);
        }
    }

    private byte[] readCluster(int clusterNum) {
        byte[] bytesOfCluster = new byte[BPB_BytsPerSec];
        int firstByteOfCluster = (clusterNum + firstDataSector - 2) * BPB_BytsPerSec;
        for (int i = firstByteOfCluster; i < firstByteOfCluster + BPB_BytsPerSec; i++) {
            bytesOfCluster[i-firstByteOfCluster] = imageBytes[i];
        }
        return bytesOfCluster;
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

    public FAT32Directory getRootElement() {
        return rootElement;
    }
}
