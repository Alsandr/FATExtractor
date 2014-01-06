package com.gmail.asandrc.extractor;

import com.gmail.asandrc.data.FAT32DIRElement;
import com.gmail.asandrc.data.FAT32Directory;
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
 * Реализований FAT32Extractor для просмотра содержимого и извлечения файлов системы FAT32
 * @author Саша
 */
public class FAT32Extractor extends BaseExtractor {
    
    protected final static int END_OF_CLUSTER_CHAIN_FAT32 = 0x0FFFFFF8;
    private static final Integer DIR_ELEMENT_SIZE = 32;
    
    private byte[] bytesOfF32DIRElement;
    
    /**
     * Метод инициализации FAT32Extractor-а
     * получение байтов файла (образа) и информации о файловой системе
     * @throws IOException 
     */
    @Override
    protected void init() throws IOException {
        typeOfFileSystem = "FAT32";        // топор
        endOfClusterchain = END_OF_CLUSTER_CHAIN_FAT32;
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
        searchFAT32Element();
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
    
    /**
     * Поиск записей файлов в системе
     * @throws UnsupportedEncodingException 
     */
    public boolean searchFAT32Element() throws UnsupportedEncodingException {
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
        buildHierarchy(rootElement, firstDataByte);
        return true;
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
    
    public byte[] extractFile(FAT32DIRElement f32DIRElement) {
        int countFileClusters = 0;
        if (f32DIRElement.getDIR_FileSize() < BPB_BytsPerSec) {
            countFileClusters = 1;
        } else {
            countFileClusters = f32DIRElement.getDIR_FileSize() / BPB_BytsPerSec + 1;
        }
        //счетчик кластеров файла для правильной записи байтов в выходной массив bytesOfF32DIRElement
        int c = 0;
        bytesOfF32DIRElement = new byte[BPB_BytsPerSec * countFileClusters];
        // считывание первый кластер файла
        byte[] bTemp = readCluster(f32DIRElement.getDIR_FstClusFULL());
        for (int i = 0; i < BPB_BytsPerSec; i++) {
            bytesOfF32DIRElement[i + c * BPB_BytsPerSec] = bTemp[i];
        }
        // вычисление смещения в таблице FAT
        calcThisFATEntOffset(f32DIRElement.getDIR_FstClusFULL());
        byte[] bCheckEOF = new byte[4];
        int beginOffset = thisFATSecNum * BPB_BytsPerSec + thisFATEntOffset;
        int endOffset = thisFATSecNum * BPB_BytsPerSec + thisFATEntOffset + 4;
        for (int j = beginOffset; j < endOffset; j++) {
            bCheckEOF[j - beginOffset] = imageBytes[j];
        }
        int resultEOF = byteArrayToInt(bCheckEOF);
        // если кластер не последний то продолжаем искать кластеры по таблице FAT
        // и записывать их в выходной массив байтов bytesOfF32DIRElement
        if (resultEOF < 0x0FFFFFFF) {
            do {
                c++;
                bTemp = readCluster(resultEOF);
                for (int i = 0; i < BPB_BytsPerSec; i++) {
                    bytesOfF32DIRElement[i + c * BPB_BytsPerSec] = bTemp[i];
                }
                calcThisFATEntOffset(resultEOF);
                bCheckEOF = new byte[4];
                beginOffset = thisFATSecNum * BPB_BytsPerSec + thisFATEntOffset;
                endOffset = thisFATSecNum * BPB_BytsPerSec + thisFATEntOffset + 4;
                for (int j = beginOffset; j < endOffset; j++) {
                    bCheckEOF[j - beginOffset] = imageBytes[j];
                }
                resultEOF = byteArrayToInt(bCheckEOF);
            } while (resultEOF < 0x0FFFFFFF);
        }
        return bytesOfF32DIRElement;
    }

    /**
     * Функция чтения кластера
     * @param clusterNum номер кластера
     * @return массив байтов кластера
     */
    public byte[] readCluster(int clusterNum) {
        byte[] bytesOfCluster = new byte[BPB_BytsPerSec];
        int firstByteOfCluster = (clusterNum + firstDataSector - 2) * BPB_BytsPerSec;
        for (int i = firstByteOfCluster; i < firstByteOfCluster + BPB_BytsPerSec; i++) {
            bytesOfCluster[i-firstByteOfCluster] = imageBytes[i];
        }
        return bytesOfCluster;
    }    
}
