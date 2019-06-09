import java.util.ArrayList;


import java.io.*;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


class BinaryPatcher {
    private String content;

    private final String filename;
    private Map<String, String> list_map;

    public BinaryPatcher(String filename, Map<String, String> list) throws IOException {

        this.filename = filename;
        this.list_map = list;
        initializeContent();
    }

    /*
    private int indexOf(byte[] outerArray, byte[] smallerArray) {
        for(int i = 0; i < outerArray.length - smallerArray.length+1; ++i) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i+j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }
    */

    public void patch() throws IOException {

        String originalContent = getContent();
        byte[] buffer = new byte[(int) new File(filename).length()];
        FileInputStream in = new FileInputStream(filename);
        in.read(buffer);
        in.close();

        for (String key : list_map.keySet())
        {

            final String searchPattern = convertHexStringToBinaryString(key);
            final String replacePattern = convertHexStringToBinaryString(list_map.get(key));

            if(!originalContent.contains(searchPattern))
            {
                System.out.println( "Unable Find Hex values : " + searchPattern );//opt
            }
            else
            {
                System.out.println( "Replaced All " + searchPattern + " To -> " + replacePattern);//opt
                originalContent = originalContent.replace(searchPattern, replacePattern);
            }
        }
        saveStringToFile(originalContent, filename);
    }

    public void createBackup(String filename) throws IOException {
        saveStringToFile(getContent(), filename);
    }


    private  String convertHexStringToBinaryString(String theHexString) {
       //= byte[] byteSequence = new BigInteger(theHexString, 16).toByteArray(); //== this have bad restore when null byte exist inside text

        //byte[] byteArray = theHexString.getBytes();
        char[] stringToCharArray = theHexString.toCharArray();
        char last_byte = 0;
        boolean isOdd = false;
        List<Byte> bb = new ArrayList<>();
        for(char ch : stringToCharArray)
        {
            if(isOdd)
            {
                bb.add((byte) ((Character.digit(last_byte, 16) << 4) + Character.digit(ch, 16)));
            }
            isOdd = !isOdd;
            last_byte = ch;
        }
        byte[] byteSequence = new byte[(bb).size()];
        int i =0;
        for( byte by : bb)
        {
            byteSequence[i] = by;
            i++;
        }
        return new String(byteSequence);
    }

    private void initializeContent() throws IOException {
        byte[] buffer = new byte[(int) new File(filename).length()];
        FileInputStream in = new FileInputStream(filename);
        in.read(buffer);
        in.close();
        content = new String(buffer);
    }

    private void saveStringToFile(String content, String filename) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);
        out.write(content.getBytes());
        out.close();
    }

    private String getContent() {
        return content;
    }
}

class replaceChars {

    public static void main(String args[])
    {

        //== load list
        Map<String, String> list_map = new HashMap<String, String>();

        try {
            BufferedReader in = new BufferedReader(new FileReader("list.csv"));
            String line;
            int index = 0;
            //  ArrayList<String> list = new ArrayList();
            while ((line = in.readLine()) != null) {
                String[] replacement = line.split(",");
                index++;
                if(replacement.length == 2)
                {
                    final String searchPattern = replacement[0];
                    final String replacePattern = replacement[1];
                    if(searchPattern.length() == replacePattern.length())// (opt) ignore when should change file size
                    {
                        list_map.put(searchPattern, replacePattern);//key, value // sample : "656873616E", "686920656873616E"
                    }
                    else
                    {
                        System.out.println("Search & replacement size isn't same, line: "+ index +" ignored, because file size May change");
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //== load list

        try {
            String filename = "file.fw";
            BinaryPatcher p = new BinaryPatcher(filename, list_map);//
            //= p.createBackup("org_" + filename );
            p.patch();

        } catch(IOException e) {
            System.out.println("Error in replacing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

