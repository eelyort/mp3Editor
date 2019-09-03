import java.util.*;
import java.io.*;

public class MyEditor {
//    String path;
    File directory;
    File[] files;
    Queue<File> fileQ;
    boolean broken;
    public MyEditor(String path){
//        try {
//            directory = new File(path);
//            files = directory.listFiles();
//            broken = false;
//        }
//        catch (NullPointerException e){
//            System.out.printf("File path: \"%s\" not found, exiting%n", path);
//            Frame.removeDir("");
//            broken = true;
//        }
        directory = new File(path);
        if(directory.isDirectory()){
            files = directory.listFiles();
            broken = false;
        }
        else{
            System.out.printf("File path: \"%s\" not found, exiting%n", path);
            Frame.removeDir("");
            broken = true;
        }
    }
    public String listFiles(){
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            File temp = files[i];
            int numDigits = ((files.length-1) + "").length();
            str.append(String.format("%" + numDigits + "d", i));
            str.append("| ");
            if(temp.isDirectory()){
                str.append("-");
                str.append(temp.getName());
            }
            else if(temp.isFile()){
                str.append(" ");
                str.append(temp.getName());
            }
            else{
                str.append("---");
                str.append(temp.getName());
                str.append("---");
            }
            str.append("\n");
        }
        return str.toString();
    }
    public File getFile(int num){
        if(num >= 0 && num < files.length)
            return files[num];
        else
            return null;
    }
    public void beginEditing(int startPosition) throws Exception{
        String placeholder = "<---> ";
        System.out.printf("%sBeginning edit...%n", placeholder);
        fileQ = new LinkedList<File>();
        for (int i = startPosition; i < files.length; i++) {
            File tempFile = files[i];
            String str = tempFile.getName();
//                    System.out.println(str);
            if(str.length()>=4)
                str = str.substring(str.length()-4);
//                    System.out.println(str);
            if(tempFile.isFile() && str.equals(".mp3"))
                fileQ.add(tempFile);
        }
        System.out.printf("%s%d files found and added to queue%n", placeholder, fileQ.size());

        int numFile = 0;
        int numDigits = ((fileQ.size()-1)+"").length();
        System.out.println();
        while (fileQ != null && !fileQ.isEmpty()){
            File currFile = fileQ.poll();

            boolean done = false;
            SingleEditor singleEditor = null;
            while(!done) {
                System.out.printf("%sFile #%-" + numDigits + "d is: %s%n", placeholder, numFile, currFile.getName());
                if(singleEditor == null)
                    singleEditor = new SingleEditor(currFile);
                System.out.printf("  New Values:%n%s%n", singleEditor.printVals());
                String commandIn = Frame.getCommand();
                done = singleEditor.command(commandIn);
                if (commandIn.equals(singleEditor.commands[9]))
                    fileQ = null;
            }

            System.out.println();
            numFile++;
        }
    }
}
