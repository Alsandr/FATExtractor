package asandrc.gmail.com.data;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 *
 * @author Саша
 */
public class FAT32DirectoryTreeNode extends FAT32Directory {
   
    public FAT32DirectoryTreeNode(byte[] bytes) throws UnsupportedEncodingException {
        super(bytes);
    }
        
    @Override
    public String toString() {
        return " " + shortName + "   " + DIR_CrtDate + "   " + DIR_CrtTime;
    }
}
