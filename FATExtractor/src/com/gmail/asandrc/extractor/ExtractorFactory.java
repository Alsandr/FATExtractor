package com.gmail.asandrc.extractor;

/**
 *
 * @author Als
 */
public class ExtractorFactory {
    
    protected final static int COUNT_OF_CLUSTERS_FAT12 = 4085;
    protected final static int END_OF_CLUSTER_CHAIN_FAT12 = 0x0FF8;
    
    protected final static int COUNT_OF_CLUSTERS_FAT16 = 65525;
    protected final static int END_OF_CLUSTER_CHAIN_FAT16 = 0xFFF8;
    
    protected final static int END_OF_CLUSTER_CHAIN_FAT32 = 0x0FFFFFF8;
    
    BaseExtractor extractor = null;
    
    /**
     * Определение типа файловой системы
     */
    public BaseExtractor createExtractor(int countOfClusters) {
        if (countOfClusters < COUNT_OF_CLUSTERS_FAT12) {
            System.out.println("FAT12Extractor do not implemented yet!");
            throw new RuntimeException("FAT12Extractor do not implemented yet!");
        } else if (countOfClusters < COUNT_OF_CLUSTERS_FAT16) {
            System.out.println("FAT16Extractor do not implemented yet!");
            throw new RuntimeException("FAT16Extractor do not implemented yet!");
        } else {
            extractor = new FAT32Extractor();
        }
        return extractor;
    }
}
