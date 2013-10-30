/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author ldtwo
 */
public abstract class AbstractCachedArray<LST extends Array<TYP>, TYP> implements Array<TYP> {
    /*
     * T[key] is the conceptual array
     * cache[idx] is local storage
     * T[key] = cache[a2c[key]]
     * if(T[key] is in cache) then key == c2a[a2c[key]] is true
     * idx == a2c[c2a[idx]]
     * 
     * timer[idx] is use count; when to free
     * clockPos = last index used in clock algo
     * dirty[idx] is true if needs write back
     * 
     */

    public String name = "?";
    public boolean CAN_DELETE = true;
    protected LST disk;
    protected int SIZE;
    protected Object[] cache;
    private boolean[] dirty;
    private int[] timer;
    private int[] refCount;
    private int[] keys;
    private int clockPos = 0;
    private LinkedList<Integer> freeList;
    private HashMap<Integer, Integer> indices;

    public AbstractCachedArray(LST disk, int SIZE) {
        this.SIZE = SIZE;
        this.disk = disk;
        dirty = new boolean[SIZE];
        timer = new int[SIZE];
        refCount = new int[SIZE];
        cache = new Object[SIZE];
        keys = new int[SIZE];
        indices = new HashMap<>(SIZE);
        freeList = new LinkedList<>();
        for (int key = 0; key < SIZE; key++) {
            freeList.add(key);
        }
    }

    @Override
    public int size() {
        return disk.size(); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void set(int key, TYP e) throws Exception {
        int idx;
//        System.out.printf("%s:  set(%s, %s)\n", name, key, e);
        if (indices.containsKey(key)) {
            idx = indices.get(key);
            timer[idx]++;
            System.out.printf("%s:   found: T[%s]=cache[%s]\n", name, key, e);
        } else {
            idx = allocate(key);
            System.out.printf("%s:   alloc: T[%s]=cache[%s]\n", name, key, e);
//            disk.store(key, e);
        }
        dirty[idx] = true;
        cache[idx] = e; 
    }


    @Override
    public TYP get(int key) throws Exception {
        int idx;
        if (indices.containsKey(key)) {
            idx = indices.get(key);
            timer[idx]++;
            System.out.printf("%s: get: found: cache[%s] <-- T[%s]\n", name, idx, key);
            return (TYP) cache[idx];
        } else {
            idx = allocate(key);
            load(key, idx);
            System.out.printf("%s: get: loaded: cache[%s] <-- T[%s]\n", name, idx, key);
            return (TYP) cache[idx];
        }
    }

    @Override
    public void shutdown() throws Exception {
        for (int idx = 0; idx < SIZE; idx++) {
            free(idx);
        }
    }

    /**
     * Make space for T[key] and put in cache[idx]
     *
     * @param key
     */
    int allocate(int key) throws Exception {
        int idx;
        
        if (indices.containsKey(key))return indices.get(key);
//        System.out.printf("%s:   cache[]={%s}\n", name, Arrays.toString(cache));
        if (freeList.size() <= 0) {//nothing free
            //run clock sweep
            idx = chooseVictim();
            System.out.printf("%s:  evicting ([%s])\n", name, idx);
            free(idx);
        } else {
        }
            idx = freeList.pop();
//            System.out.printf("%s:     popped ([%s])\n", name, idx);
//        System.out.printf("%s:     mapped: cache[%s] <-- T[%s]\n", name, idx, key);
        indices.put(key, idx);
        dirty[idx] = false;//duplicated work
        timer[idx] = 2;
        keys[idx] = key;
        return idx;
    }

    /**
     * Handles clock sweep code.
     *
     * @return
     */
    int chooseVictim() {
        int victim = -1;
        int attempts = SIZE * 4;
        for (int j = 0; j < SIZE; j++) {
//            System.out.printf("%s:     clock: time(cache[%s])=%s\n", name,
//                    (j + clockPos) % SIZE,timer[(j + clockPos) % SIZE]);
            if (timer[(j + clockPos) % SIZE] == 0 && refCount[(j + clockPos) % SIZE] == 0) {
                victim = (j + clockPos) % SIZE;
                break;
            }
        }
        if (victim < 0) {
            for (int j = 0;attempts-->0; j++) {
//            System.out.printf("%s:     clock2: time(cache[%s])=%s\n", name,
//                    (j + clockPos) % SIZE,timer[(j + clockPos) % SIZE]);
                if (timer[(j + clockPos) % SIZE] <= 0 && refCount[(j + clockPos) % SIZE] == 0) {
                    victim = (j + clockPos) % SIZE;
                    break;
                } else if (timer[(j + clockPos) % SIZE] > 0) {//pre dec
                    timer[(j + clockPos) % SIZE]--;
                }
            }
        }
        clockPos = victim + 1;
//        new Exception().printStackTrace(System.out);
        return victim;
    }

    /**
     * Free and write back element at cache[idx] = T[key]
     *
     * @param idx
     */
    void free(int idx) throws Exception {
        //if dirty[idx], then move cache[idx] from L to L-1
        //else delete cache[idx] 
        //add idx to free list
        if (dirty[idx]) {
            //write back(key, T[key])==wb(c2a[idx], cache[idx])
            store(keys[idx], (TYP) cache[idx]);
        } 
        cache[idx] = null;
        keys[idx] = -1;//for safety and sanity
        dirty[idx] = false;
        freeList.push(idx);
        indices.remove(idx);
        
//        System.out.printf("%s:     push ([%s])\n", name, idx);
    }

    /**
     * Call local set() and wb(), then do LST.set(key,e) and LST.store(key,e).
     * After wb(key,e), this and all lower levels will up to date and valid.
     *
     * @param key
     * @param e
     */
    @Override
    public abstract void store(int key, TYP e) throws Exception;

    /**
     * Call LST.get() and then store in local array Assumes that after load(key,
     * idx) is called, the value T[key] will be in cache[idx] and cache[idx] will
     * be valid (if other operations are necessary, such as file copy or
     * decompression).
     *
     * @param key
     * @param idx
     */
    @Override
    public abstract void load(int key, int idx) throws Exception;

    public static void main(String[] args) {
    }

    @Override
    public void add(TYP e) throws Exception {
        int key = size();
        System.out.printf("%s: adding(%s, %s)\n", name, key, e);
        set(key, e);
    }
}
