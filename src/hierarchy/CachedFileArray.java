package hierarchy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.*;

/**
 *
 * @author ldtwo
 */
public class CachedFileArray extends AbstractCachedArray<Array<File>, File> {

    File L0, L1;

    public CachedFileArray(File L0, File L1, Array<File> disk, int SIZE) {
        super(disk, SIZE);
        this.L0 = L0;
        this.L1 = L1;
        for (File f : L0.listFiles()) {
            System.out.printf("%s:  cleaning(%s): %s\n", name, L0.getName(), f);
            f.delete();
        }
    }

    @Override
    void free(int idx) throws Exception {
        File f = ((File) cache[idx]);
//        super.free(idx); //do all the normal work

        //if dirty[idx], then move cache[idx] from L to L-1
        //else delete cache[idx] 
        //add idx to free list
//        if (dirty[idx])
        {
            //write back(key, T[key])==wb(c2a[idx], cache[idx])
            store(keys[idx], f);
        }
        cache[idx] = null;
        keys[idx] = -1;//for safety and sanity
        dirty[idx] = false;
        freeList.push(idx);
        if (CAN_DELETE) {

            f.delete();//free this space
//            Thread.sleep(100);
//            System.out.printf("%s:  DELETE(%d, %s)\n", name, idx, f.getName());
        }
        if (L0.listFiles().length > capacity()) {
            System.err.printf(getName() + "ERROR(st self): Cache is over loaded after store! %s\n",
                    "");
            new Exception().printStackTrace();
            System.err.println(toString());
            System.exit(-1);
        }
        if (L1.listFiles().length > getChild().capacity()) {
            System.err.printf(getChild().getName() + "ERROR(st sub): Cache is over loaded after store! %s\n",
                    "");
            new Exception().printStackTrace();
            System.err.println(toString());
            System.exit(-1);
        }
    }

    @Override
    public void store(int key, File src) throws Exception {
        //future improvement: call wb() for all children from here
        //Need linked access as a list
        Path dest = Paths.get(L1.getAbsolutePath(), src.getName());
//        System.out.printf("%s:     wb: T[%s] %s ==> %s\n", name, key, src, dest);
        if (!src.exists()) {
//            dest.toFile().delete();
            System.err.printf("ERROR(wb): File missing! %s\n",
                    src.getAbsolutePath());
            if (!src.exists()) {
                System.err.printf("WARNING(wb): File missing! %s\n",
                        src.getAbsolutePath());
            }
            if (dest.toFile().exists()) {
                System.err.printf("WARNING(wb): File already exists! %s\n",
                        dest);
            }
            new Exception().printStackTrace();
            System.err.println(toString());
            System.exit(-1);
//            Thread.sleep(100);
        }
        //perform local write back: L --> (L-1)
        //copy(source, dest, replace?)
//        System.err.printf("%s:  STORE(%s)  %s ==> %s\n", name, src.getName(),
//                src.getParentFile().getName(), dest.toFile().getParentFile().getName());
        //update this level so that we know it exists
//        System.out.printf("%s:  this.SET(%s, %s)\n", name,key,e.getName());
//        this.set(key, e);//prob not correct
//        System.out.printf("%s:  %s.SET(%s, %s)\n", name,disk.getName(), key, dest.toFile().getName());
//        disk.store(key,  dest.toFile());//tell lower level to write back
        int tries = 1;
        while (true) {
            try {
                Files.copy(src.toPath(), dest, REPLACE_EXISTING);
                if (!dest.toFile().exists()) {
                    System.err.printf("ERROR(wb): Copy failed! %s\n", dest);
                    if (!src.exists()) {
                        System.err.printf("WARNING(wb): File missing! %s\n",
                                src.getAbsolutePath());
                    }
                    if (dest.toFile().exists()) {
                        System.err.printf("WARNING(wb): File already exists! %s\n",
                                dest);
                    }
                    new Exception().printStackTrace();
                    System.err.println(toString());
                    System.exit(-1);
                }
                break;
            } catch (IOException ex) {
//                Thread.sleep(1);
                if (tries-- < 0) {
                    System.err.printf("ERROR: could not copy file!\n\tFROM: %s\n\tTO: %s\n",
                            src, dest);
                    if (!src.exists()) {
                        System.err.printf("WARNING(wb): File missing! %s\n",
                                src.getAbsolutePath());
                    }
                    if (dest.toFile().exists()) {
                        System.err.printf("WARNING(wb): File already exists! %s\n",
                                dest);
                    }
                    new Exception().printStackTrace();
                    System.err.println(toString());
                    break;
                }
            }
        }
//           System.err.println("\nBEFORE STORE");
int idx;
//        print();
        idx = allocate(key);
        File f = ((File) cache[idx]);
        if (CAN_DELETE) {
            f.delete();//free this space
        }

//        cache[idx] = dest.toFile();
//        System.err.println("AFTER");
//        print();
//        Thread.sleep(1);
        
        disk.set(key, dest.toFile());//update lower level

//        if (L0.listFiles().length > capacity()) {
//            System.err.printf(getName() + "ERROR(st self): Cache is over loaded after store! %s\n",
//                    dest);
//            new Exception().printStackTrace();
//            System.err.println(toString());
//            System.exit(-1);
//        }
//        if (L1.listFiles().length > getChild().capacity()) {
//            System.err.printf(getChild().getName() + "ERROR(st sub): Cache is over loaded after store! %s\n",
//                    dest);
//            new Exception().printStackTrace();
//            System.err.println(toString());
//            System.exit(-1);
//        }
    }

    @Override
    public void load(int key, int idx) throws Exception {
        Array<File> child = disk;
        while (!child.containsKey(key) && child != null) {
            child = child.getChild();
        }
//        System.out.printf("%s:     loading: cache[%s] <-- T[%s] %s <== %s\n", name, idx, key, "", "");
        File src = child.get(key);
        Path dest = Paths.get(L0.getAbsolutePath(), src.getName());
        if (dest.toFile().exists()) {
            System.err.printf("WARNING(load): Leak detected! File already exists: %s\n",
                    dest.toFile().getAbsolutePath());
//            Thread.sleep(100);
        }
        //perform local fetch: L <-- (L-1)
        //copy(source, dest, replace?)
//        System.err.printf("%s:  LOAD(%s)  %s ==> %s\n", name, src.getName(),
//                src.getParentFile().getName(), dest.toFile().getParentFile().getName());



        int tries = 1;
        while (true) {
            try {
                Files.copy(src.toPath(), dest);
                if (!dest.toFile().exists()) {
                    System.err.printf("ERROR(load): Copy failed! \n\t%s\n\t%s\n", src, dest);
                    System.exit(-1);
                }
                break;
            } catch (Exception ex) {
                Thread.sleep(1);
                if (tries-- < 0) {
                    System.err.printf("ERROR: could not copy file!\n\tFROM: %s\n\tTO: %s\n",
                            src, dest);
                    if (!src.exists()) {
                        System.err.printf("WARNING(load): File missing! %s\n",
                                src.getAbsolutePath());
                    }
                    if (dest.toFile().exists()) {
                        System.err.printf("WARNING(load): File already exists! %s\n",
                                dest);
                    }
                    System.err.println(toString());
                    ex.printStackTrace();
                    break;
                }
            }
        }
//        System.err.println("\nBEFORE");

//        print();
//        File f = ((File) cache[idx]);
        idx = allocate(key);
//        if (CAN_DELETE) {
//            f.delete();//free this space
//        }

        cache[idx] = dest.toFile();
//        System.err.println("AFTER");
//        print();
//        Thread.sleep(1);

        if (!dest.toFile().exists()) {
            System.err.printf("WARNING(load): Copy failed! \n\tsrc(%s): %s\n", src.exists(), src.getAbsolutePath());
            System.err.printf("\ndest(%s): %s\n", dest.toFile().exists(), dest);
            System.err.println(toString());
            System.exit(-1);
        }
        if (L0.listFiles().length > capacity()) {
            System.err.printf(getName() + ": ERROR(ld self): Cache is over loaded after load! %s\n",
                    dest);
            System.err.printf(getChild().getName() + ": %s > %s \n",
                    L0.listFiles().length, capacity());
            new Exception().printStackTrace();
            System.err.println(toString());
            System.exit(-1);
        }
        if (L1.listFiles().length > getChild().capacity()) {
            System.err.printf(getChild().getName() + ": ERROR(ld sub): Cache is over loaded after load! %s\n",
                    dest);
            System.err.printf(getChild().getName() + ": %s > %s \n",
                    L1.listFiles().length, getChild().capacity());
            new Exception().printStackTrace();
            System.err.println(toString());
            System.exit(-1);
        }
    }

    @Override
    public void add(File e) throws Exception {

        Path dest = Paths.get(L0.getAbsolutePath(), e.getName());
        if (dest.toFile().exists()) {
            System.err.printf("WARNING(wb): Leak detected! File already exists: %s\n",
                    dest.toFile().getAbsolutePath());
        }
        //perform local write : L --> (L-1)
        //copy(source, dest, replace?)
        Files.copy(e.toPath(), dest, REPLACE_EXISTING);
        //update this level so that we know it exists
        super.add(e);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Array<File> getChild() {
        return disk;
    }

    @Override
    public int capacity() {
        return CAPACITY;
    }
}
