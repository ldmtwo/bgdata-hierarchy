/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storageserver;

import java.util.Arrays;

/**
 *
 * @author ldtwo
 */
public class Definition {
   public long hash=0;
   public byte[] definition=null;

    public Definition() {
    }

    public Definition(long hash, byte[] definition) {
        this.hash = hash;
        this.definition = definition;
    }

    @Override
    public int hashCode() {
        return (int) hash; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object o) {
        Definition o2=(Definition) o;
        return Arrays.equals(definition, o2.definition); //To change body of generated methods, choose Tools | Templates.
    }
   
}
