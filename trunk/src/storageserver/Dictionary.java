/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storageserver;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author ldtwo
 */
public class Dictionary extends HashMap<Long, Chapter> {
    HashMap<Long, Chapter> book=new HashMap<>(8);
    
    public boolean add(Definition d){
        Chapter chpt=book.get(d.hash);
        
        return chpt.add(d);
    }

    @Override
    public int hashCode() {
        return book.hashCode(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o); //To change body of generated methods, choose Tools | Templates.
    }
    
}
