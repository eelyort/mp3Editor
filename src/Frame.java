import java.util.*;
import java.io.*;

public class Frame {
    private static String path;
    private static BufferedReader s;

    private static MyEditor editor;

    private static String[] commands;
    private static String[] helpText;

    private static boolean toggleList;

    public static void main(String[] args) throws Exception {

        path = "D:\\Troy\\Audio";
        s = new BufferedReader(new InputStreamReader(System.in));

//        String string = "Mother Monsterct";
//        System.out.printf("Test: indexOf is %d%n", string.indexOf("Monstercat"));
//        String string = "Mother (hi) and (hi there)";
//        StringTokenizer st = new StringTokenizer(string);
//        while (st.hasMoreTokens())
//            System.out.println(st.nextToken());
//        File file = new File(path);
//        System.out.println(file.getPath());
//        System.out.println(file.toString());

        initHelp();

        toggleList = true;

        while (true) {
            System.out.printf("Current Path is:  %s%n", path);
            editor = new MyEditor(path);
            if(!editor.broken) {
                if (toggleList) {
                    System.out.printf("Files:%n" +
                            "%s", editor.listFiles());
                }
                String str = getCommand();
                command(str);
            }
        }
    }

    protected static String command(String str) throws Exception{
//        str = str.toLowerCase();
        try {
            StringTokenizer st = new StringTokenizer(str);
            String temp = st.nextToken().toLowerCase();
            if (temp.equals(commands[0])) {
                help();
            } else if (temp.equals(commands[1])) {
                if (st.hasMoreTokens())
                    removeDir(str.substring(commands[1].length() + 1));
                else
                    removeDir("");
            } else if (temp.equals(commands[2])) {
                if (st.hasMoreTokens())
                    addDir(str.substring(commands[2].length() + 1));
                else
                    addDir("");
            } else if (temp.equals(commands[3])) {
                if (str.equals(commands[3]))
                    editor.beginEditing(0);
                else
                    editor.beginEditing(Integer.parseInt(str.substring(commands[3].length() + 1)));
            } else if (temp.equals(commands[4])) {
                toggleList = !toggleList;
            } else {
                System.out.printf("Invalid Command Inputted: %s%n", str);
            }
            return "";
        }
        catch (Exception e){
            System.out.printf("Exception: %s---------------------------------%n" +
                    "at Frame command() str = %s%n", e.toString(), str);
            return "";
        }
    }

    public static void removeDir(String str){
        int num;
        try{
            num = Integer.parseInt(str);
        }
        catch(NumberFormatException e){
            System.out.printf("NumberFormatException at removeDir%n");
            num = 1;
        }
        catch (NullPointerException e){
            System.out.printf("NullPointerException at removeDir%n");
            num = 1;
        }
        for (int i = 0; i < num; i++) {
            removeDir2();
        }
    }
    private static void removeDir2(){
        for (int i = path.length()-1; i > 0; i--) {
            if(path.charAt(i) == '\\'){
                path = path.substring(0, i);
                break;
            }
        }
    }

    public static void addDir(String str) throws Exception{
//        System.out.printf("addDir called, enter dir to add: ");
//        String temp = s.readLine();
//        path = path + "\\" + temp;
        if(str.length() == 0)
            return;
        try{
            int num = Integer.parseInt(str);
            addDir2(editor.getFile(num).getName());
        }
        catch(NumberFormatException e){
            System.out.println("NumberFormatException at addDir");
            addDir2(str);
        }
        catch (NullPointerException e){
            System.out.println("NullPointerException at addDir");
        }
    }
    public static void addDir2(String str){
        path = path + "\\" + str;
    }

    private static void initHelp(){
        commands = new String[5];
        commands[0] = "help";
        commands[1] = "-";
        commands[2] = "+";
        commands[3] = "edit";
        commands[4] = "toggleList";
        for (int i = 0; i < commands.length; i++) {
            commands[i] = commands[i].toLowerCase();
        }

        helpText = new String[commands.length];
        helpText[0] = String.format("\"%s\" - Lists possible commands", commands[0]);
        helpText[1] = String.format("\"%s\" - Removes the last dir", commands[1]);
        helpText[2] = String.format("\"%s\" - Adds a new dir to the end", commands[2]);
        helpText[3] = String.format("\"%s\" - Begins editing all files in directory", commands[3]);
        helpText[4] = String.format("\"%s\" - Toggles file listing", commands[4]);
    }

    private static void help(){
        System.out.println("List of possible commands:");
        for (int i = 0; i < helpText.length; i++) {
            System.out.printf(" %s%n", helpText[i]);
        }
    }

    public static String getCommand() throws Exception{
        return s.readLine();
    }
}
