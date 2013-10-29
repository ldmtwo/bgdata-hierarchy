/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storageserver;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author ldtwo
 */
public class Chapter extends ArrayList<Definition>{
    public int hash;

    public Chapter(int hash) {
        this.hash = hash;
    }
    
    ArrayList<Definition> definitions=new ArrayList<>(2);
   public boolean add(Definition d){
       int index= find(d);
       if (index<0 ){
        definitions.add(d);
        return true;
        }
        return false;
   }
   public int find(Definition d){
       int index=-1;
       for(int i=0;i<definitions.size();i++){
           if(Arrays.equals(d.definition, definitions.get(i).definition)){
               index=i;
               break;
           }
       }
       return index;
   }

    @Override
    public int hashCode() {
        return hash; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object o) {
        Chapter ch=(Chapter) o;
        return this.definitions.equals(ch.definitions); //To change body of generated methods, choose Tools | Templates.
    }
    
   
}
