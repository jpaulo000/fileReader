package ufal.ic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import java.util.logging.*;

public class CSVReader implements PropertyChangeListener {

	// Variables used to handle file and set the progress of the task in the
	// main window
	public static String[] header = { "" };
	public static long bytesInTheFile = 0;
	public static long currentLineByte = 1;
	public static double percent = 0.0;
	public static long currentRow = 1;
	private boolean isHeaderDefined = false;
	private String filename;

	// Logger
	private Logger log = Logger.getLogger("Log");

	// Set this variable manually to identify the fields
	private String FIELD_SEPARATOR;
	// Null counter for each field
	private Map<String, Integer> dict = new HashMap<>();

	public CSVReader() {
		// constructor
		
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

		try {
			/// Prepara as variaveis
			BufferedReader buffer = new BufferedReader(new FileReader(filepath));
			String fileheader;

			// Prerapação do arquivo de log
			FileHandler fh = new FileHandler("D:\\javalog.txt");
			log.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

			// Prepara o cabecalho
			fileheader = buffer.readLine();
			setFieldSeparator(fileheader);
			if (!FIELD_SEPARATOR.equals("")) {
				System.out.println("Achou separador");
				header = fileheader.split(FIELD_SEPARATOR);
				MainWindow.setVarList(header);

				// Iniciailiza o dicionario
				for (int i = 0; i < header.length; i++) {
					dict.put(header[i], 0);
				}

				isHeaderDefined = true;
				File file = new File(filepath);
				bytesInTheFile = file.length();
				System.out.println(bytesInTheFile);

			} else {
				MainWindow.showErrorMessagePopUp("Field separator not identified.\nOpen source file and define one.");
				log.severe("Field separator not identified.\nOpen source file and define one.");

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

	public void readFileContent(String filepath) {
		if (isHeaderDefined) {
			new Thread() {
				public void run() {
					try {
						/// Declaration and instantiation of the variables;
						BufferedReader buffer = new BufferedReader(new FileReader(filepath));
						String line;
						// Discard the file line (header of the file)
						line = buffer.readLine();

						while ((line = buffer.readLine()) != null) {
							// Update the percentage
							percent = ((double) currentLineByte / bytesInTheFile) * 100.0;
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
								log.info("Row " + currentRow + " has " + (header.length - splintered.length)
										+ " more fields than the header.");

							} else if (splintered.length == header.length) {
								for (int i = 0; i < splintered.length; i++) {
									if (splintered[i].equals("")) {
										dict.put(header[i], dict.get(header[i]) + 1);
									}
								}

								// if the line has lesser fields, continue from
								// where it ends and fill the rest with nulls.
							} else if (splintered.length < header.length) {
								for (int newBegin = splintered.length; newBegin < header.length; newBegin++)
									dict.put(header[newBegin], dict.get(header[newBegin]) + 1);
							}
							
							if(currentRow % 500000 == 0){
								log.info("Read row " + currentRow + ".");
							}
							// Useless for coding purposes but if the file is
							// too big (and I mean in the GBs territory)
							// this is a good indicator that it's still running
							//System.out.println(new Date(System.currentTimeMillis()) + " - Read " + currentLineByte + " of "
							//		+ bytesInTheFile);
							currentLineByte += line.getBytes().length;
							currentRow++;

						}
						buffer.close();
					} catch (FileNotFoundException e) {
						System.out.println("File not found.");
						e.printStackTrace();
					} catch (IOException e) {
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

	public void setFieldSeparator(String header) {
		if (header.contains(","))
			FIELD_SEPARATOR = ",";
		else if (header.contains(";"))
			FIELD_SEPARATOR = ";";
	}

	public void printMap(Map<String, Integer> map) {
		Iterator<?> it = map.entrySet().iterator();
		System.out.println("Nulls found in each field");
		try {
			FileWriter file = new FileWriter(filename + "_output.txt");
		} catch (IOException e) {
			System.out.println("File already exists.");
			e.printStackTrace();
		}
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			System.out.println(pair.getKey() + " - " + pair.getValue());
			it.remove();
		}

	}

}
