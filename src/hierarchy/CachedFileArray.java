package hierarchy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static java.nio.file.StandardCopyOption.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        super.free(idx); //do all the normal work   
        if (CAN_DELETE) {
            f.delete();//free this space 
            System.out.printf("%s:  DELETE(%d, %s)\n", name, idx, f.getName());
        }
    }

    @Override
    public void store(int key, File src) throws Exception {
        //future improvement: call wb() for all children from here
        //Need linked access as a list
        Path dest = Paths.get(L1.getAbsolutePath(), src.getName());
//        System.out.printf("%s:     wb: T[%s] %s ==> %s\n", name, key, e, dest);
        if (!src.exists()) {
//            dest.toFile().delete();
            System.err.printf("WARNING(wb): File missing! %s\n",
                    src.getAbsolutePath());
            Thread.sleep(100);
        }
        //perform local write back: L --> (L-1)
        //copy(source, dest, replace?)
        Files.copy(src.toPath(), dest, REPLACE_EXISTING);
        System.out.printf("%s:  STORE(%s)  %s ==> %s\n", name, src.getName(),
                src.getParentFile().getName(), dest.toFile().getParentFile().getName());
        //update this level so that we know it exists
//        System.out.printf("%s:  this.SET(%s, %s)\n", name,key,e.getName());
//        this.set(key, e);//prob not correct
        System.out.printf("%s:  %s.SET(%s, %s)\n", name,disk.getName(), key, dest.toFile().getName());
//        disk.store(key,  dest.toFile());//tell lower level to write back
        disk.set(key, dest.toFile());//update lower level
    }

    @Override
    public void load(int key, int idx) throws Exception {
//        System.out.printf("%s:     loading: cache[%s] <-- T[%s] %s <== %s\n", name, idx, key, "", "");
        File src = disk.get(key);
        Path dest = Paths.get(L0.getAbsolutePath(), src.getName());
        if (dest.toFile().exists()) {
            System.err.printf("WARNING(load): Leak detected! File already exists: %s\n",
                    dest.toFile().getAbsolutePath());
//            Thread.sleep(100);
        }
        //perform local fetch: L <-- (L-1)
        //copy(source, dest, replace?)
        System.out.printf("%s:  LOAD(%s)  %s ==> %s\n", name, src.getName(),
                src.getParentFile().getName(), dest.toFile().getParentFile().getName());
        try {
            Files.copy(src.toPath(), dest, REPLACE_EXISTING);
        } catch (IOException ex) {
            Files.copy(src.toPath(), dest, REPLACE_EXISTING);
        }

        cache[idx] = dest.toFile();
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
}
