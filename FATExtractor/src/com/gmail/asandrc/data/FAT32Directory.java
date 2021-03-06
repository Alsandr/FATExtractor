package com.gmail.asandrc.data;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс записи директории в файловой системе FAT32
 * @author Саша
 */
public class FAT32Directory extends FAT32DIRElement {
    
    protected List<FAT32DIRElement> childElements;
    protected List<FAT32Directory> childDirectories;
    
    /**
     * Конструктор
     * @param bytes
     * @throws UnsupportedEncodingException 
     */
    public FAT32Directory(byte[] bytes) throws UnsupportedEncodingException {
        super(bytes);
        childElements = new ArrayList<FAT32DIRElement>();
        childDirectories = new ArrayList<FAT32Directory>();
    }
    
    /**
     * Метод для установки короткого имени (будет использоваться ТОЛЬКО 
     * для задания имени КОРНЕВОГО элемента FAT32 обаза)
     * @param newShortName устанавливаемое имя
     */
    public void setShortName(String newShortName) {
        shortName = newShortName;
    }

    /**
     * Метод получения списка FAT32DIRElement
     * @return список FAT32DIRElement
     */
    public List<FAT32DIRElement> getChildElements() {
        return childElements;
    }

    /**
     * Метод получения списка FAT32Directory (директорий)
     * @return список FAT32Directory
     */
    public List<FAT32Directory> getChildDirectories() {
        return childDirectories;
    }
    
    @Override
    public String toString() {
        return shortName;
    }
}
