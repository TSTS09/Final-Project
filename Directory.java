import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.TreeMap;

class File {
    private String modified_time;
    private String created_time;
    private String name;
    private boolean is_dir;

    public File(String name) {
        this.name = name;
        this.is_dir = false;
        this.created_time = getDateTime();
        this.modified_time = getDateTime();
    }

    private String getDateTime() {
        String time = LocalTime.now().toString();
        time = time.substring(0, time.indexOf("."));
        return LocalDate.now().toString() + " " + time;
    }

    public String getModified_time() {
        return modified_time;
    }

    public void upModified_time() {
        this.modified_time = getDateTime();
    }

    public String getCreated_time() {
        return created_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean Is_dir() {
        return is_dir;
    }

    protected void setIs_dir(boolean is_dir) {
        this.is_dir = is_dir;
    }

    public PrintStream info() {
        return System.out.format("%-5s %-20s %-3s %s\n", "file", modified_time, "", name);
    }
}

class Directory extends File {
    private HashMap<String, File> container;
    private int count;

    public Directory(String name) {
        super(name);
        setIs_dir(true);
        container = new HashMap<>();
        count = 0;
    }

    public void add(File file) {
        container.put(file.getName(), file);
        count++;
    }

    public void delete(String name) {
        container.remove(name);
        count--;
    }

    public File find(String name) {
        return container.get(name);
    }

    public void print() {
        for (File file : container.values()) {
            file.info();
        }
    }

    public PrintStream info() {
        return System.out.format("%-5s %-20s %-3s %s\n", "dir", getModified_time(), count, getName());
    }

    // get all files in this directory
    public Collection<File> getContainer() {
        return container.values();
    }

    public boolean containsFile(String fileName) {
        return Is_dir() && container != null && container.containsKey(fileName);
    }
}

class FileTree<T extends File> {
    private TreeMap<String, T> tree;

    public FileTree() {
        tree = new TreeMap<>();
    }

    public void add(T file) {
        tree.put(file.getName(), file);
    }

    public void remove(String name) {
        tree.remove(name);
    }

    public T get(String name) {
        return tree.get(name);
    }

    private void print(File file, String indent) {
        if (indent.length() > 0) {
            System.out.print("|");
        }

        System.out.print(indent + "|-- ");
        System.out.println(file.getName());
        if (file.Is_dir()) {
            for (File f : ((Directory) file).getContainer()) {
                print(f, indent + " ".repeat(4));
            }
        }
    }

    public void printFileTree() {
        for (File file : tree.values()) {
            print(file, "");
        }
    }

    public List<String> findFilePath(String fileName) {
        List<String> path = new ArrayList<>();
        for (T file : tree.values()) {
            if (dfsSearch(file, fileName, path)) {
                return path;
            }
        }
        return null; // File not found
    }

    public boolean dfsSearch(File file, String fileName, List<String> path) {
        path.add(file.getName());

        if (file.getName().equals(fileName)) {
            return true; // File found
        }

        if (file.Is_dir()) {
            for (File f : ((Directory) file).getContainer()) {
                if (dfsSearch(f, fileName, path)) {
                    return true;
                }
            }
        }

        path.remove(path.size() - 1); // Backtrack if not found in this subtree
        return false;
    }

    public void delete(String name) {
        for (T file : tree.values()) {
            if (dfsSearch(file, name)) {
                if (file instanceof Directory) {
                    removeFileOrDirectory((Directory) file, name);
                } else {
                    tree.remove(name);
                }
                break; // Break the loop after removing the file or directory with the specified name
            }
        }
    }

    private void removeFileOrDirectory(Directory directory, String name) {
        for (File file : directory.getContainer()) {
            if (file.getName().equals(name)) {
                directory.getContainer().remove(file);
                return;
            } else if (file instanceof Directory) {
                removeFileOrDirectory((Directory) file, name);
            }
        }
    }

    public boolean dfsSearch(File file, String fileName) {
        if (file.getName().equals(fileName)) {
            return true; // File found
        }

        if (file.Is_dir()) {
            for (File f : ((Directory) file).getContainer()) {
                if (dfsSearch(f, fileName)) {
                    return true;
                }
            }
        }
        return false;
    }
}

class Main {
    public static void main(String[] args) {
        FileTree<File> tree = new FileTree<>();

        File file1 = new File("file1");
        File file2 = new File("file2");
        Directory dir3 = new Directory("dir3");

        tree.add(file1);
        tree.add(file2);
        tree.add(dir3);

        dir3.add(new File("file3"));
        dir3.add(new File("file4"));

        Directory dir2 = new Directory("dir2");
        dir3.add(dir2);

        dir2.add(new File("file5"));
        dir2.add(new File("file6"));

        tree.printFileTree();
        List<String> filePath = tree.findFilePath("dir2");

        if (filePath != null) {
            System.out.println("File found at path: " + String.join("/", filePath));
        } else {
            System.out.println("File not found in the directory tree.");
        }

        System.out.println();
        System.out.println();

        tree.delete("dir2");
        tree.printFileTree();

    }
}
