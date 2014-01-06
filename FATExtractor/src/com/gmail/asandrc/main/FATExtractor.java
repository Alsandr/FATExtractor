package com.gmail.asandrc.main;

import com.gmail.asandrc.extractor.FAT32Extractor;
import com.gmail.asandrc.view.MainFrame;
import java.io.IOException;
import javax.swing.UIManager;

/**
 * Главный класс программы
 * @author Саша
 */
public class FATExtractor {

    private static MainFrame mainFrame;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {    
        mainFrame = new MainFrame();
        mainFrame.setLocation(300,200);
        mainFrame.setVisible(true);
    }
}
