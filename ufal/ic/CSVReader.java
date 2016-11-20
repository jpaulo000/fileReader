package ufal.ic;

import sun.applet.Main;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class CSVReader{

	// Variables used to handle file and set the progress of the task in the
	// main window
    public static String[] header = {""};
    public static String[] watchlist = {""};
    public static long bytesInThisFile = 0;
    public static long currentByteSum;
    public static int totalLines = 1;
    public static double percent = 0.0;
    private boolean isHeaderDefined = false;
    private String filename;

	// Logger
	private Logger log = Logger.getLogger("Log");

	// Set this variable manually to identify the fields
	private String FIELD_SEPARATOR;
	// Null counter for each field
    private Map<String, Integer> nullMap = new HashMap<>();
    private Map<String, Integer> headerID = new HashMap<>();
    private List<FieldAttributes> watchlisted = new ArrayList<>();

	public CSVReader() {
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
//			FileHandler fh = new FileHandler("D:\\javalog.txt");
//			log.addHandler(fh);
//			SimpleFormatter formatter = new SimpleFormatter();
//			fh.setFormatter(formatter);

			// Prepara o cabecalho
			fileheader = buffer.readLine();
			setFieldSeparator(fileheader);
			if (!FIELD_SEPARATOR.equals("")) {
				header = fileheader.split(FIELD_SEPARATOR);
				MainWindow.setVarList(header);

				// Iniciailiza o dicionario
				for (int i = 0; i < header.length; i++) {
                    nullMap.put(header[i], 0);
                    headerID.put(header[i], i);
				}

                totalLines++;
                isHeaderDefined = true;
                bytesInThisFile = new File(filepath).length();
				System.out.println(bytesInThisFile);

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
            this.watchlist = watchlist;

            for(int i = 0; i < watchlist.length; i++){
                watchlisted.add(new FieldAttributes(watchlist[i]));
            }
            try {
                /// Declaration and instantiation of the variables;
                BufferedReader buffer = new BufferedReader(new FileReader(filepath));
                String line;
                // Discard the header of the file
                line = buffer.readLine();

                int currentRow = 1;
                while ((line = buffer.readLine()) != null) {
                    // Update the percentage
                    percent = ((double) currentByteSum / bytesInThisFile) * 100.0;
                    new Thread() {
                        public void run() {
                            // Update progress bar in another thread so
                            // it doesnt freeze the interface
                            MainWindow.progressBar.setValue((int) CSVReader.percent);
                        }
                    }.start();

                    String[] splintered = line.split(FIELD_SEPARATOR);

                    // If the line has more fields than the header save
                    // it in a log
                    if (splintered.length > header.length) {
//								log.info("Row " + currentRow + " has " + (splintered.length - header.length)
//										+ " more fields than the header.");

                    } else if(splintered.length == header.length){
                        for(int index = 0; index < splintered.length; index++){
                            if(splintered[index].equals("")){
                                nullMap.put(header[index], nullMap.get(header[index])+1);
                            }else{
                                checkWatchListed(index, splintered[index]);
                            }
                        }

                        // if the line has lesser fields, continue from
                        // where it ends and fill the rest with nulls.
                    } else if (splintered.length < header.length) {
                        for (int newBegin = splintered.length; newBegin < header.length; newBegin++)
                            nullMap.put(header[newBegin], nullMap.get(header[newBegin]) + 1);
                    }

                    if(currentRow % 500000 == 0){
                        //log.info("Read row " + currentRow + ".");
                    }
                    currentByteSum += line.getBytes().length;
                    currentRow++;
                    totalLines++;

                }

                percent = 100.0;
                MainWindow.progressBar.setValue((int)percent);
                buffer.close();

            }catch (FileNotFoundException e) {
                MainWindow.showErrorMessagePopUp("File not found.");
                e.printStackTrace();
            }catch (IOException e){
                MainWindow.showErrorMessagePopUp("Error opening the file.");
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
            //log.severe("Field separator not identified.\nOpen source file and define one.");
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
            printWatchList(file, watchlisted);
            file.close();
		} catch (IOException e) {
            MainWindow.showErrorMessagePopUp("File already exists.");
			e.printStackTrace();
		}
        map.clear();
        watchlisted.clear();

	}

	public void printWatchList(PrintWriter file, List<FieldAttributes> watchlisted){
		for(FieldAttributes each : watchlisted)
			file.println(each.printAttributes());
	}

	public void checkWatchListed(int index, String field_value){
		//Is this field being observed?
		for(int i = 0; i < watchlist.length; i++){
			if(watchlist[i].equals(header[index])){
                System.out.println("Inserindo item "+field_value+" na watchlist");
				watchlisted.get(i).addFieldAttributes(field_value);
			}
		}
	}

}
