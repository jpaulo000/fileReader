package ufal.ic;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;


public class CSVReader implements PropertyChangeListener{

    //Variables used to handle file and set the progress of the task in the main window
    public static String[] header = {""};
    public static long linesInThisFile = 0;
    public static long currentLine = 1;
    public static double percent = 0.0;
    private boolean isHeaderDefined = false;
    private String filename;

    //Set this variable manually to identify the fields
    private String FIELD_SEPARATOR;
    //Null counter for each field
    private Map<String, Integer>dict = new HashMap<>();

    public CSVReader(){
        //constructor
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            MainWindow.progressBar.setValue(progress);
        }
    }


    public void readFileHeader(String filepath, String filename) {
        this.filename = filename;
        try{
            ///Prepara as variaveis
            FileReader reader = new FileReader(filepath);
            BufferedReader buffer = new BufferedReader(reader);
            String line, fileheader;
            //Prepara o cabecalho
            fileheader = buffer.readLine();
            setFieldSeparator(fileheader);
            if(!FIELD_SEPARATOR.equals("")){
                System.out.println("Achou separador");
                header = fileheader.split(FIELD_SEPARATOR);
                MainWindow.setVarList(header);

                //Iniciailiza o dicionario
                for(int i = 0; i < header.length; i++){
                    dict.put(header[i], 0);
                }

                isHeaderDefined = true;
                File file = new File(filepath);
                linesInThisFile = file.length();
                System.out.println(linesInThisFile);

            }else{
                MainWindow.showErrorMessagePopUp("Field separator not identified.\nOpen source file and define one.");
            }

            reader.close();
            buffer.close();

        }catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error opening the file.");
            e.printStackTrace();
        }
    }


    public void readFileContent(String filepath) {
        if(isHeaderDefined){
            new Thread(){
                public void run(){
                    try{
                        ///Declaration and instantiation of the variables;
                        FileReader reader = new FileReader(filepath);
                        BufferedReader buffer = new BufferedReader(reader);
                        String line;
                        //Discard the file line (header of the file)
                        line = buffer.readLine();

                        while((line = buffer.readLine())!=null){
                            //Update the percentage
                            percent = ((double)currentLine/linesInThisFile) * 100.0;
                            new Thread(){
                                public void run(){
                                    //Update progress bar in another thread so it doesnt freeze the interface
                                    MainWindow.progressBar.setValue((int)CSVReader.percent);
                                }
                            }.start();

                            String[] splintered = line.split(FIELD_SEPARATOR);

                            //If the line has more fields than the header save it in a log
                            if(splintered.length > header.length){
//                                System.out.println("Line " + currentLine + " contains " + splintered.length + " fields.");
//                                for(String each : splintered)
//                                    System.out.print(each + " | ");
                                //TODO: Salvar as linhas com formataçao diferente em outro campo

                            //If the line is perfectly filled just do its job
                            }else if(splintered.length == header.length){
                                for(int i = 0; i < splintered.length; i++){
                                    if(splintered[i].equals("")){
                                        dict.put(header[i], dict.get(header[i])+1);
                                    }
                                }

                            //if the line has lesser fields, continue from where it ends and fill the rest with nulls.
                            }else if(splintered.length < header.length){
                                for(int newBegin = splintered.length; newBegin < header.length;newBegin++)
                                    dict.put(header[newBegin], dict.get(header[newBegin])+1);
                            }
                            //Useless for coding purposes but if the file is too big (and I mean in the GBs territory)
                            // this is a good indicator that it's still running
                            System.out.println(new Date(System.currentTimeMillis())+" - Read "+currentLine+ " of "+linesInThisFile);
                            currentLine += line.getBytes().length;

                        }
                        reader.close();
                        buffer.close();
                    }catch (FileNotFoundException e) {
                        System.out.println("File not found.");
                        e.printStackTrace();
                    }catch (IOException e){
                        System.out.println("Error opening the file.");
                        e.printStackTrace();
                    }
                    printMap(dict);
                }
            }.start();
        }

    }

    public int countLines(String filepath) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filepath));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    public void setFieldSeparator(String header){
        if(header.contains(","))
            FIELD_SEPARATOR = ",";
        else if(header.contains(";"))
            FIELD_SEPARATOR = ";";
    }

    public void printMap(Map map){
        Iterator it = map.entrySet().iterator();
        System.out.println("Nulls found in each field");
        try {
            FileWriter file = new FileWriter(filename+"_output.txt");
        } catch (IOException e) {
            System.out.println("File already exists.");
            e.printStackTrace();
        }
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " - " + pair.getValue());
            it.remove();
        }

    }


}
