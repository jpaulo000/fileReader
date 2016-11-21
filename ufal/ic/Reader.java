package ufal.ic;

import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class Reader {

    // Variables used to handle file and set the progress of the task in the
    // main window
    public static String[] header = {""};
    public static String[] watchlist_fieldnames = {""};
    public static long bytesInThisFile = 0;
    public static long currentByteSum = 0;
    public static int total_num_lines = 0;
    public static double percent = 0.0;
    private boolean isHeaderDefined = false;
    private String filename;

    // Logger
    private Logger log = Logger.getLogger("Log");

    // Set this variable manually to identify the fields
    private String FIELD_SEPARATOR;
    // Null counter for each field
    private Map<String, Integer> nullMap = new HashMap<>();
    private List<FieldAttributes> watchlist_values_mapper = new ArrayList<>();

    public Reader() {
        // constructor

    }

    public void readFileHeader(String filepath) {
        String[] splitPath = filepath.split("\\\\");
        filename = splitPath[splitPath.length-1].split("\\.")[0];
        MainWindow.progressBar.setValue(0);

        try {
            /// Prepara as variaveis
            BufferedReader buffer = new BufferedReader(new FileReader(filepath));
            String fileheader;

            // Prerapacao do arquivo de log
            FileHandler fh;
            try {
                // This block configure the logger with handler and formatter
                fh = new FileHandler(filepath.split("\\.")+"_LOG.txt");
                log.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);

                // the following statement is used to log any messages
                log.info("LOG from "+filename);

            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Prepares the header
            fileheader = buffer.readLine();
            setFieldSeparator(fileheader);
            if (!FIELD_SEPARATOR.equals("")) {
                header = fileheader.split(FIELD_SEPARATOR);
                MainWindow.setVarList(header);

                // Initializes the hashmap with zeroes nulls found in each field
                for (int i = 0; i < header.length; i++) {
                    nullMap.put(header[i], 0);
                }

                total_num_lines++;
                isHeaderDefined = true;
                bytesInThisFile = new File(filepath).length();
            }

            buffer.close();

        } catch (FileNotFoundException e) {
            log.severe("File " + filepath + " not found!");
            e.printStackTrace();
        } catch (IOException e) {
            log.severe("Error opening the file " + filepath + ".");
            e.printStackTrace();
        }
    }

    public void readFileContent(String filepath, String[] watchlist) {

        if(isHeaderDefined && !nullMap.isEmpty()){
            watchlist_fieldnames = watchlist;

            for(int i = 0; i < watchlist_fieldnames.length; i++){
                watchlist_values_mapper.add(new FieldAttributes(watchlist_fieldnames[i]));
            }

            // Update progress bar in another thread so
            // it doesnt freeze the interface
            new Thread() {
                public void run() {
                    MainWindow.progressBar.setValue((int) Reader.percent);
                }
            }.start();

            try {

                CSVReader reader = new CSVReader(new FileReader(new File(filepath)));
                //discard header
                String[] line = reader.readNext();
                //Start processing the next lines
                while ((line = reader.readNext())!= null) {
                    for(int index = 0; index < line.length; index++){
                        if(line[index].equals(""))
                            nullMap.put(header[index], nullMap.get(header[index]+1));
                        else
                            checkWatchListed(index, line[index]);
                    }
                    String completeLine = Arrays.toString(line);
                    currentByteSum += completeLine.getBytes().length;
                    percent = ((double) currentByteSum / bytesInThisFile) * 100.0;
                    total_num_lines++;
                }
                log.info("Finished running");
                percent = 100.0;
                MainWindow.progressBar.setValue((int)percent);

            }catch (FileNotFoundException e) {
                MainWindow.showErrorMessagePopUp("File not found.");
                log.severe("File not found");
                e.printStackTrace();
            }catch (IOException e){
                MainWindow.showErrorMessagePopUp("Error opening the file.");
                log.severe("File not found");
                e.printStackTrace();
            }
            printMap(nullMap);
            MainWindow.StartButton.setEnabled(true);
        }

    }

    public void setFieldSeparator(String header) {
        if (header.contains(","))
            FIELD_SEPARATOR = ",";
        else if (header.contains(";"))
            FIELD_SEPARATOR = ";";
        else
            MainWindow.showErrorMessagePopUp("Field separator not identified.\nOpen source file and define one.");
    }

    public void printMap(Map<String, Integer> map) {
        Iterator it = map.entrySet().iterator();

        try {

            PrintWriter file = new PrintWriter(new FileWriter(filename + "_output.txt"));
            file.println("\tNULLS FOUND");
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                file.println(pair.getKey() + "\t\t|\t" + pair.getValue()+" nulls.");
            }
            it.remove();
            printWatchList(file, watchlist_values_mapper);
            file.close();
        } catch (IOException e) {
            MainWindow.showErrorMessagePopUp("File already exists.");
            e.printStackTrace();
        }
        map.clear();
        watchlist_values_mapper.clear();

    }

    public void printWatchList(PrintWriter file, List<FieldAttributes> watchlisted){
        for(FieldAttributes each : watchlisted)
            file.println(each.printAttributes());
    }

    public void checkWatchListed(int index, String field_value){
        //Is this field being observed?
        for(int i = 0; i < watchlist_fieldnames.length; i++){
            if(watchlist_fieldnames[i].equals(header[index])){
                watchlist_values_mapper.get(i).addFieldAttributes(field_value);
            }
        }
    }
}
