/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy;

import java.io.File;

/**
 *
 * @author ldtwo
 */
public class ArrayList<T> implements Array<T> {

    public String name = "?";
    public boolean CAN_DELETE = true;
    java.util.ArrayList<T> data = new java.util.ArrayList<>();

    @Override
    public String toString() {
        return ("\n"+data.toString()).replace(", ", "\n");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

    @Override
    public T get(int key) {
        return data.get(key);
    }

    @Override
    public void set(int key, T e) {
        if (data.size() <= key) {
            data.add(key, e);
        } else {
            data.set(key, e);
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public void add(T e) {
        data.add(e);
    }

    @Override
    public void store(int key, T e) throws Exception {
        //only need to update locally; no other work to do
        set(key, e);
    }

    @Override
    public void load(int key, int idx) throws Exception {
        //no where to load from since this is the lowest level
    }

    @Override
    public boolean containsKey(int key) {
        return data.size() > key;
    }

    @Override
    public void shutdown() throws Exception {
        //save anything that needs saving
    }

    @Override
    public Array<File> getChild() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int capacity() {
        return size();
    }
}
