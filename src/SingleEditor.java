import java.util.*;
import java.io.*;
import com.mpatric.mp3agic.*;

public class SingleEditor {
    private final String defaultValue = "Unknown";

    File file;
    Mp3File mp3;

    String title = defaultValue;
//    Set<String> artists = new LinkedHashSet<String>();
    String artist = defaultValue;
    String album = defaultValue;

    static String[] commands;
    private static String[] helpText;

    public SingleEditor(File file) throws Exception{
        initSets();
        this.file = file;
        initHelp();

        mp3 = new Mp3File(file.getPath());

        if(mp3.hasId3v2Tag()) {
            ID3v2 tag = mp3.getId3v2Tag();
            System.out.printf("Init SingleEditor: tag ver = %s%n", tag.getVersion());

            String temp = tag.getTitle();
            if(temp != null) {
                title = temp;
            }

            temp = tag.getArtist();
            if(temp != null){
                artist = temp;
            }

            temp = tag.getAlbum();
            if(temp != null){
                album = temp;
            }
        }

        System.out.printf("  Original Vals: %n%s%n", printVals());
        title = defaultValue;
        artist = defaultValue;
        album = defaultValue;
        parse();
        //TODO
    }

    public String printVals(){
        StringBuilder str = new StringBuilder();
        String temp = "";

        temp = String.format("    Title: %s%n", title);
        str.append(temp);

//        str.append("  Artists:\n");
//        Iterator<String> it = artists.iterator();
//        while(it.hasNext()){
//            String currArtist = it.next();
//            temp = String.format("    %s,%n", currArtist);
//            str.append(temp);
//        }

        temp = String.format("    Artist: %s%n", artist);
        str.append(temp);

        temp = String.format("    Album: %s", album);
        str.append(temp);

        return str.toString();
    }

    public boolean command(String input) throws Exception{
        try {
            StringTokenizer st = new StringTokenizer(input);
            String temp = st.nextToken().toLowerCase();
            if (temp.equals(commands[0])) {
                help();
            } else if (temp.equals(commands[1])) {
                changeTitle(input.substring(commands[1].length() + 1));
            }
//        else if(temp.equals(commands[2])){
//            addArtist(input.substring(commands[2].length() + 1));
//        }
//        else if(temp.equals(commands[3])){
//            if(!removeArtist(input.substring(commands[3].length() + 1)))
//                System.out.println("Artist not found");
//        }
            else if (temp.equals(commands[4])) {
                changeAlbum(input.substring(commands[4].length() + 1));
            } else if (temp.equals(commands[5])) {
                finish();
                return true;
            } else if (temp.equals(commands[6])) {
                return true;
            } else if (temp.equals(commands[7])) {
                parse();
            } else if (temp.equals(commands[8])) {
                changeArtist(input.substring(commands[8].length() + 1));
            } else if (temp.equals(commands[9])) {
                return true;
            } else {
                System.out.printf("Invalid Command Inputted: %s%n", input);
            }
            return false;
        }
        catch (Exception e){
            System.out.printf("Exception: %s-------------------------------------------------%n" +
                    " while taking in command %s-------------------------------------------%n", e.toString(), input);
            System.out.printf("Invalid Command Inputted: %s%n", input);
            return false;
        }
    }

    private void parse(){
        try {
            String str = file.getName();
            str = str.substring(0, str.length() - 4);
//                System.out.println(str);
            char tempC = str.charAt(0);
            if (tempC == '(')
                parseAudio(str);
            else if (tempC == '[')
                parseJap(str);
            else if (tempC == '{')
                parseOther(str);
            else
                parseAmer(str);
        }
        catch (Exception e){
            System.out.printf("Exception: %s-------------------------------------------------%n" +
                    " while parsing file %s-------------------------------------------%n", e.toString(), file.getName());
//            title = defaultValue;
//            artist = defaultValue;
//            album = defaultValue;
        }
    }

    private void parseAudio(String str) throws Exception{

        //title
        int indexOfLastBracket = -1;
        for (int i = 0; i < str.length(); i++) {
            if(str.charAt(i) == ')'){
                indexOfLastBracket = i;
                i = str.length();
            }
        }
        title = str.substring(0, indexOfLastBracket+1);
        str = str.substring(indexOfLastBracket+4);
//        System.out.printf("parseAudio: %s%n", str);

        //artist
        String tempArtist = "";
        StringTokenizer st = new StringTokenizer(str);
        while(st.hasMoreTokens()){
            String token = st.nextToken();
            if(audioAlbums.containsKey(token)){
                //Album Ex: (Monstercat)
                album = audioAlbums.get(token);
            }
            else if(token.equals("OST")){
                //Album Ex: Yuru Camp OST
                album = tempArtist.substring(0,tempArtist.length()-1);
                tempArtist = "";
            }
            else{
                tempArtist += token + " ";
            }
        }
        if(tempArtist != "")
            artist = tempArtist.substring(0,tempArtist.length()-1);

//        //album
//        Iterator it = audioAlbums.keySet().iterator();
//        while (it.hasNext()){
//            String key = (String)it.next();
//            int index = str.indexOf(key);
//            if(index != -1){
//                album = audioAlbums.get(key);
//                str = removePiece(str, index, key.length());
//            }
//        }
//
//        //artist
//        artist = str;
    }

    private void parseJap(String str) throws Exception{

        //title
        int indexOfLastBracket = -1;
        for (int i = 0; i < str.length(); i++) {
            if(str.charAt(i) == ']'){
                indexOfLastBracket = i;
                i = str.length();
            }
        }
        title = str.substring(0, indexOfLastBracket+1);
        str = str.substring(indexOfLastBracket+5);
//                System.out.printf("parseJap: \"%s\"%n", str);

        //artist
        int indexOfDash = str.indexOf('-');
        if(indexOfDash == -1) {
            artist = str;
            str = "";
        }
        else{
            artist = str.substring(0, indexOfDash-1);
            str = str.substring(indexOfDash+2);
        }

        //album and OP, ED, OST, etc
        boolean sourcesFoundYet = false;
        StringTokenizer st = new StringTokenizer(str);
        String tempAlbum = "";
        while (st.hasMoreTokens()){
            String token = st.nextToken();

            //checking for OP#, ED#, OST, S#, etc
            boolean bool = false;
            Iterator<String> it = sourceMarkers.keySet().iterator();
            while (it.hasNext()){
                String key = it.next();
                //If key inside token and length us correct
                if(token.contains(key) && token.length() <= sourceMarkers.get(key)){
                    if(!sourcesFoundYet){
                        title += " - ";
                        sourcesFoundYet = true;
                    }
                    else{
                        ;
                    }
                    title += token + " ";
                    bool = true;
                }
            }

            if(!bool){
                tempAlbum += token + " ";
            }
        }
        //remove spaces
        if(sourcesFoundYet && title.length()>0){
            title = title.substring(0, title.length()-1);
        }
        if(tempAlbum.length() > 0 && !tempAlbum.equals(defaultValue)){
            album = tempAlbum.substring(0, tempAlbum.length()-1);
        }

    }

    private void parseOther(String str) throws Exception{

    }

    private void parseAmer(String str) throws Exception{
        //title
        int indexDash = -1;
        for (int i = 0; i < str.length(); i++) {
            if(str.charAt(i) == '-'){
                indexDash = i;
                i = str.length();
            }
        }
        title = str.substring(0, indexDash-1);
        str = str.substring(indexDash+5);

        //artist
        StringTokenizer st = new StringTokenizer(str);
        String tempArtist = "";
        while (st != null && st.hasMoreTokens()){
            String token = st.nextToken();
            if(token.contains("ft") || token.contains("feat") || token.contains("lyrics") || token.contains("Lyrics") || token.contains("LYRICS")){
                st = null;
            }
            else{
                tempArtist += token + " ";
            }
        }
        if(tempArtist.length() > 0)
            artist = tempArtist.substring(0, tempArtist.length()-1);
    }

    private void changeTitle(String str){
        title = str;
    }

//    private void addArtist(String str){
//        artists.add(str);
//    }
//
//    private boolean removeArtist(String str){
//        return artists.remove(str);
//    }

    private void changeArtist(String str){
        artist = str;
    }

    private void changeAlbum(String str){
        album = str;
    }

    private void finish() throws Exception{
        //TODO: code for saving
//        mp3.removeCustomTag();

//        ID3v1 tag1 = mp3.getId3v1Tag();
//        tag1.setTitle(title);
//        tag1.setArtist(artist);
//        tag1.setAlbum(album);
//        mp3.setId3v1Tag(tag1);

//        ID3v2 tag2 = mp3.getId3v2Tag();
        ID3v2 tag2;
        tag2 = new ID3v24Tag();
//        mp3file.setId3v2Tag(id3v2Tag);
//                System.out.printf("Finish method, tag2 = %s%n", tag2.toString());
//                System.out.printf(" title = %s, artist = %s, album = %s%n", tag2.getTitle(), tag2.getArtist(), tag2.getAlbum());
        tag2.setTitle(title);
        tag2.setArtist(artist);
        tag2.setAlbum(album);
//        ID3v2 realTag = ID3v2TagFactory.createTag(tag2.toBytes());
        mp3.setId3v2Tag(tag2);
//                System.out.printf("Finish method, tag2 = %s%n", tag2.toString());
//                System.out.printf(" title = %s, artist = %s, album = %s%n", tag2.getTitle(), tag2.getArtist(), tag2.getAlbum());
//        System.out.printf("Finish method: %n" +
//                " hasCustomTag() = %s, hasTag1() = %s, hasTag2() = %s%n", mp3.hasCustomTag(), mp3.hasId3v1Tag(), mp3.hasId3v2Tag());

        String path = file.getPath();                   //First part: EX: D:\\Troy\\Coding\\IntelliJ\\mp3Editor\\TestDir\\Songs 10
        for (int i = path.length()-1; i > 0; i--) {
            if(path.charAt(i) == '\\'){
                path = path.substring(0, i);
                i = 0;
            }
        }

        path += "\\EditedTags\\";

        File tempFile = new File(path);
        if(!tempFile.exists()){
            tempFile.mkdir();
        }

        String str = file.getName();
//        str = str.substring(0, str.length() - 4);
        path += str;

        try {
//            mp3.save(file.getName());
            mp3.save(path);
        }
        catch (Exception e){
            System.out.printf("Exception: %s----------------------------------------------%n" +
                    "at finish method of file %s%n", e.toString(), file.getName());
        }

//                tag2 = mp3.getId3v2Tag();
//                System.out.printf("End Finish method, tag2 = %s%n", tag2.toString());
//                System.out.printf(" title = %s, artist = %s, album = %s%n", tag2.getTitle(), tag2.getArtist(), tag2.getAlbum());
//                System.out.printf("Finish method: %n" +
//                        " hasCustomTag() = %s, hasTag1() = %s, hasTag2() = %s%n", mp3.hasCustomTag(), mp3.hasId3v1Tag(), mp3.hasId3v2Tag());
    }

    private void initHelp(){
        commands = new String[10];
        commands[0] = "help";
        commands[1] = "cTitle";
        commands[2] = "+Art";
        commands[3] = "-Art";
        commands[4] = "cAlb";
        commands[5] = "fin";
        commands[6] = "skip";
        commands[7] = "parse";
        commands[8] = "cArt";
        commands[9] = "quit";
        for (int i = 0; i < commands.length; i++) {
            commands[i] = commands[i].toLowerCase();
        }

        helpText = new String[commands.length];
        helpText[0] = String.format("\"%s\" - Lists possible commands", commands[0]);
        helpText[1] = String.format("\"%s\" - Changes the title", commands[1]);
        helpText[2] = String.format("[Depreciated]\"%s\" - Adds a new artist", commands[2]);
        helpText[3] = String.format("[Depreciated]\"%s\" - Removes an artist", commands[3]);
        helpText[4] = String.format("\"%s\" - Changes the album", commands[4]);
        helpText[5] = String.format("\"%s\" - Saves and goes to next", commands[5]);
        helpText[6] = String.format("\"%s\" - Goes to next without saving", commands[6]);
        helpText[7] = String.format("\"%s\" - Parses title, artist, album from file name", commands[7]);
        helpText[8] = String.format("\"%s\" - Changes the artist", commands[8]);
        helpText[0] = String.format("\"%s\" - Exits the editing chain", commands[0]);
    }

    private void help(){
        System.out.println("List of possible commands:");
        for (int i = 0; i < helpText.length; i++) {
            System.out.printf(" %s%n", helpText[i]);
        }
    }

    private final Map<String, String> audioAlbums = new LinkedHashMap<String, String>();
    private final Map<String, Integer> sourceMarkers = new LinkedHashMap<String, Integer>();
    private void initSets(){

        //Audio albums
        audioAlbums.put("(Monstercat)", "Monstercat");

        //Source markers: value is length to remove
        sourceMarkers.put("OST", 3);
        sourceMarkers.put("OP", 3);
        sourceMarkers.put("ED", 3);
        sourceMarkers.put("S", 2);
        sourceMarkers.put("insert", 7);
        sourceMarkers.put("Insert", 7);
        sourceMarkers.put("Movie", 5);
        sourceMarkers.put("movie", 5);
    }

    private String removePiece(String str, int indexToRemove, int lengthToRemove){
        if(str.length()-indexToRemove <= lengthToRemove) {
            if(str.charAt(indexToRemove-1) == ' ')
                return str.substring(0, indexToRemove-1);
            else
                return str.substring(0, indexToRemove);
        }
        else{
            if(str.charAt(indexToRemove-1) == ' ')
                return str.substring(0, indexToRemove-1) + str.substring(indexToRemove+lengthToRemove);
            else
                return str.substring(0, indexToRemove) + str.substring(indexToRemove+lengthToRemove);
        }
    }
}
