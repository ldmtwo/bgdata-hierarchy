package hierarchy;

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
    protected int CAPACITY;
    protected Object[] cache;
    protected boolean[] dirty;
    protected int[] timer;
    private int[] refCount;
    protected int[] keys;
    private int clockPos = 0;
    protected LinkedList<Integer> freeList;
    protected HashMap<Integer, Integer> indices;

    @Override
    public String toString() {
        String ret = "";
        ret += String.format("Name: %s\n", name);
        ret += String.format("\tdisk: %s\n", disk.getName());
        ret += String.format("\tFREE/CAPACITY: %s/%s\n", freeList.size(), CAPACITY);
        ret += String.format("\tCache: dirty\ttimer\tkeys\tcache\n\t\t%s\t%s\t%s\t%s\n", dirty[0] ? "1" : "0", timer[0], keys[0], cache[0]);
        for (int i = 1; i < CAPACITY; i++) {
            ret += String.format("\t\t%s\t%s\t%s\t%s\n", dirty[i] ? "1" : "0", timer[i], keys[i], cache[i]);
        }
        ret += String.format("\tclockPos: %s\n", clockPos);
        ret += String.format("\tfreeList: %s\n", freeList);
        ret += disk.toString().replace("\n", "\n" + disk.getName() + "\t");

        return ret;
    }

    public void print() {
        String ret = "";
        ret += String.format("Name: %s\n", name);
        ret += String.format("\tdisk: %s\n", disk.getName());
        ret += String.format("\tFREE/CAPACITY: %s/%s\n", freeList.size(), CAPACITY);
        ret += String.format("\tCache: dirty\ttimer\tkeys\tcache\n\t\t%s\t%s\t%s\t%s\n", dirty[0] ? "1" : "0", timer[0], keys[0], cache[0]);
        for (int i = 1; i < CAPACITY; i++) {
            ret += String.format("\t\t%s\t%s\t%s\t%s\n", dirty[i] ? "1" : "0", timer[i], keys[i], cache[i]);
        }
        ret += String.format("\tclockPos: %s\n", clockPos);
        ret += String.format("\tfreeList: %s\n", freeList);
        System.err.println(ret);
    }

    public void printKeys() {
        String ret = getName() + " [";
        for (int i = 0; i < CAPACITY; i++) {
            ret += String.format("%4s,", keys[i]);
        }
        ret += "]";
//        ret+="\t"+disk.printKeys();
        System.err.println(ret);
    }

    public AbstractCachedArray(LST disk, int CAPACITY) {
        this.CAPACITY = CAPACITY;
        this.disk = disk;
        dirty = new boolean[CAPACITY];
        timer = new int[CAPACITY];
        refCount = new int[CAPACITY];
        cache = new Object[CAPACITY];
        keys = new int[CAPACITY];
        indices = new HashMap<>(CAPACITY);
        freeList = new LinkedList<>();
        for (int key = 0; key < CAPACITY; key++) {
            freeList.add(key);
        }
    }

    @Override
    public boolean containsKey(int key) {
        return indices.containsKey(key);
    }

    @Override
    public int size() {
        return disk.size(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set(int key, TYP e) throws Exception {
        int idx;

        idx = allocate(key);
        timer[idx]++;
        dirty[idx] = true;
        cache[idx] = e;
    }

    @Override
    public TYP get(int key) throws Exception {
        int idx;
        if (!indices.containsKey(key)) {
            load(key, -1);
        }
        idx = indices.get(key);
        timer[idx]++;
        return (TYP) cache[idx];
    }

    @Override
    public void shutdown() throws Exception {
        for (int idx = 0; idx < CAPACITY; idx++) {
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
//System.err.printf(getName()+": alloc:\n\tkey=%s (%s)\n\t# free=%s\n", key, indices.containsKey(key)
//        ,freeList.size()
//        );
        if (indices.containsKey(key)) {//key already owns a spot
//System.err.printf("\tidx=%s\n", indices.get(key)        );
            return indices.get(key);
        }
//        if (freeList.contains(key)) {
//            System.out.printf("WARNING: Why is key \"%s\" free?\n", key);
//            System.exit(-1);
//        }
        if (freeList.size() <= 0) {//nothing free
            //run clock sweep
            idx = chooseVictim();//choose someone
            //evict someone
            store(keys[idx], (TYP) cache[idx]);
            cache[idx] = null;
            keys[idx] = -1;//for safety and sanity
            dirty[idx] = false;
            //freeList.push(idx);
        } else {
            idx = freeList.pop();
        }
        indices.put(key, idx);
        if (cache[idx] != null) {
            System.out.printf("ERROR: cache[%s] not null!\n", idx);
            System.exit(-1);
        }
//System.err.printf("\tidx=%s\n", idx        );
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
//        return clockPos;
        int victim = -1;
        int attempts = CAPACITY * 4;
        for (int j = 0; j < CAPACITY; j++) {
//            System.out.printf("%s:     clock: time(cache[%s])=%s\n", name,
//                    (j + clockPos) % CAPACITY,timer[(j + clockPos) % CAPACITY]);
            if (timer[(j + clockPos) % CAPACITY] == 0) {
                victim = (j + clockPos) % CAPACITY;
                break;
            }
        }
        if (victim < 0) {
            for (int j = 0;; j++) {
//            System.out.printf("%s:     clock2: time(cache[%s])=%s\n", name,
//                    (j + clockPos) % CAPACITY,timer[(j + clockPos) % CAPACITY]);
                if (timer[(j + clockPos) % CAPACITY] <= 0) {
                    victim = (j + clockPos) % CAPACITY;
                    break;
                } else if (timer[(j + clockPos) % CAPACITY] > 0) {//pre dec
                    timer[(j + clockPos) % CAPACITY]--;
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
//        if (dirty[idx])
        {
            //write back(key, T[key])==wb(c2a[idx], cache[idx])
            store(keys[idx], (TYP) cache[idx]);
//            printKeys();
        }
        cache[idx] = null;
        keys[idx] = -1;//for safety and sanity
        dirty[idx] = false;
        freeList.push(idx);
//        indices.remove(idx);

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
     * idx) is called, the value T[key] will be in cache[idx] and cache[idx]
     * will be valid (if other operations are necessary, such as file copy or
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
//        System.out.printf("%s: adding(%s, %s)\n", name, key, e);
        set(key, e);
    }
}
