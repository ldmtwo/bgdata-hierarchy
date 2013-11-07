/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author ldtwo
 */
public interface Array<TYP> {

    public String getName();
 
    public TYP get(int key) throws Exception;

    public void set(int key, TYP e) throws Exception;

    public int size();
    public int capacity();

    public void shutdown() throws Exception;

    public void add(TYP e) throws Exception;
    public boolean containsKey(int key);
    public Array<File> getChild();
    /**
     * Call local set() and wb(), then do LST.set(key,e) and
     * LST.store(key,e). After wb(key,e), this and all lower levels will up
     * to date and valid.
     *
     * @param key
     * @param e
     */
    public abstract void store(int key, TYP e) throws Exception;

    /**
     * Call LST.get() and then store in local array Assumes that after load(key,
     * idx) is called, the value T[key] will be in cache[idx] and cache[idx]
     * will be valid (if other operations are necessary, such as file copy or
     * decompression).
     *
     * @param key
     * @param idx
     */
    public abstract void load(int key, int idx) throws Exception;
}
