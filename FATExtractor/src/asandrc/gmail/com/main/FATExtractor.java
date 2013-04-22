package asandrc.gmail.com.main;

import asandrc.gmail.com.extractor.FAT32Extractor;
import asandrc.gmail.com.view.MainFrame;
import java.io.IOException;
import javax.swing.UIManager;

/**
 *
 * @author Саша
 */
public class FATExtractor {

    private static MainFrame mainFrame;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
//        try {
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//        } catch(Exception e) {
//            e.printStackTrace();
//        }        
        mainFrame = new MainFrame();
        mainFrame.setLocation(300,200);
        mainFrame.setVisible(true);
    }
}
