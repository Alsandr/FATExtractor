package asandrc.gmail.com.extractor;

import asandrc.gmail.com.data.FAT32DIRElement;
import java.io.File;

/**
 * Базовый класс FATExtractor для реализации FAT32Extractor
 * и в будущем для FAT16Extractor
 * @author Саша
 */
public abstract class BaseExtractor {
    
    /**
     * Открытие файла (образа) с системой FAT
     * (получение массива байтов файла)
     * @param FATfile фалй (образ)
     */
    public abstract boolean openFAT(File FATfile);
    
    /**
     * Метод извлечения файла
     * @param f32DIRElement f32DIRElement файла, который необходимо извлечь
     * @return массив байтов файла
     */
    public abstract byte[] extractFile(FAT32DIRElement f32DIRElement);
    
}
