package ufal.ic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.opencsv.CSVReader;

public class GroupBy {

	private static ArrayList<String> valuesList = new ArrayList<String>();
	private static int headerLength;
	private static int oldHeaderLength;
	private static int[] possibleColumnsValues;
	private static String[] finalHeader;
	private static Logger log = Logger.getLogger("Log");
	private static final int rowPreviewNumber = 100000;

	private String actualFile;

	public Map<String, ArrayList<Integer>> groupValues(CSVReader csv, Boolean isPreview) throws IOException {

		String[] header = csv.readNext();
		headerLength = header.length;
		oldHeaderLength = headerLength;

		finalHeader = setRow(header, possibleColumnsValues);
		headerLength = finalHeader.length;

		String[] oldRow;
		int rowcount = 0;

		Map<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();

		while ((oldRow = csv.readNext()) != null) {

			String[] newRow = setRow(oldRow, possibleColumnsValues);

			if (isPreview)
				getUniqueValues(newRow);

			for (int j = 0; j < valuesList.size(); j++) {
				String actualValue = valuesList.get(j);
				ArrayList<Integer> actualsum = countOcurrences(newRow, actualValue);

				if (!map.containsKey(actualValue))
					map.put(actualValue, actualsum);
				else {
					ArrayList<Integer> oldsum = map.get(actualValue);
					ArrayList<Integer> newsum = new ArrayList<Integer>();
					for (int i = 0; i < headerLength; i++) {
						if (actualsum.get(i) != 0)
							newsum.add(i, (oldsum.get(i)) + 1);
						else
							newsum.add(i, oldsum.get(i));
					}
					map.replace(actualValue, oldsum, newsum);
				}

			}
			if (rowcount % 500000 == 0 && isPreview == false) {
				log.info("Lida linha " + rowcount);
			}
			if (rowcount == rowPreviewNumber && isPreview == true)
				break;

			rowcount++;
		}
		valuesList.clear();
		if (!isPreview)
			log.info("Análise concluída para o arquivo" + actualFile);
		return map;
	}

	private String[] setRow(String[] oldRow, int[] columns) {
		ArrayList<String> newRowList = new ArrayList<String>();

		for (int i = 0; i < oldHeaderLength; i++)

			for (int j = 0; j < columns.length; j++)
				if (i == columns[j]) {
					newRowList.add(oldRow[i]);
				}
		String[] newRowArray = newRowList.toArray(new String[newRowList.size()]);

		return newRowArray;

	}

	public static ArrayList<Integer> countOcurrences(String[] row, String value) {
		ArrayList<Integer> sumvalor = new ArrayList<Integer>();
		for (int i = 0; i < headerLength; i++)
			sumvalor.add(0);

		for (int i = 0; i < headerLength; i++) {
			if (row[i].equals(value)) {
				int oldvalue = sumvalor.get(i);
				sumvalor.set(i, oldvalue + 1);
			}
		}

		return sumvalor;
	}

	public static void getUniqueValues(String[] row) {
		for (int i = 0; i < headerLength; i++) {
			if (!valuesList.contains(row[i]))
				valuesList.add(row[i]);
		}

	}

	/*
	 * public void generateIntegersValues(){ for(int i=0;i<17;i++)
	 * valuesList.add(Integer.toString(i)); valuesList.add(""); }
	 */

	public static void generateColumnsValues(CSVReader csv) throws IOException {
		log.info("Identificando as fields a serem analisadas.");
		int rowcount = 0;
		ArrayList<Integer> sumvalor = new ArrayList<Integer>();

		String[] header = csv.readNext();
		int intHeaderLength = header.length;

		for (int i = 0; i < intHeaderLength; i++)
			sumvalor.add(0);

		while (rowcount != rowPreviewNumber) {
			String[] row = csv.readNext();

			for (int i = 0; i < intHeaderLength; i++) {
				if (row[i].isEmpty())
					row[i] = "100";
				if (isFieldAInteger(row[i], 10)) {
					int oldvalue = sumvalor.get(i);
					sumvalor.set(i, oldvalue + 1);
				}
			}
			rowcount++;
		}

		int average = (int) sumvalor.stream().mapToInt(i -> i).average().orElse(0);
		// System.out.println("media: " + average);

		ArrayList<Integer> columns = new ArrayList<Integer>();

		for (int i = 0; i < sumvalor.size(); i++)
			if (sumvalor.get(i) > average + (average / 2))
				columns.add(i);

		possibleColumnsValues = columns.stream().mapToInt(i -> i).toArray();
		log.info("Foram identificados " + possibleColumnsValues.length
				+ " campos que podem ser analisados neste arquivo.");
	}

	public void generateFieldsToAnalyse(CSVReader csv) throws IOException {
		log.info("Iniciando leitura do arquivo " + actualFile + ".");
		generateColumnsValues(csv);
		log.info("Agrupando valores únicos em uma amostra");

		Map<String, ArrayList<Integer>> map = groupValues(csv, true);
		log.info("Encontrado " + map.size() + " possíveis valores. Verificando os mais frequentes");
		Map<String, Integer> sumUniqueValuesMap = new HashMap<String, Integer>();
		int sum = 0, totalSum = 0;

		for (Map.Entry<String, ArrayList<Integer>> entry : map.entrySet()) {
			String actualKey = entry.getKey();
			ArrayList<Integer> actualValue = entry.getValue();

			for (int d : actualValue)
				sum += d;
			sumUniqueValuesMap.put(actualKey, sum);
			totalSum += sum;
			sum = 0;
		}
		int average = totalSum / sumUniqueValuesMap.size();

		for (Entry<String, Integer> entry : sumUniqueValuesMap.entrySet())
			if (entry.getValue() >= average)
				valuesList.add(entry.getKey());

		log.info("Foram identificados " + valuesList.size() + " valores que podem ser analisados neste arquivo.");

	}

	private static Boolean isFieldAInteger(String value, int raiz) {
		if (value.isEmpty())
			return false;
		for (int i = 0; i < value.length(); i++) {
			if (i == 0 && value.charAt(i) == '-') {
				if (value.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(value.charAt(i), raiz) < 0)
				return false;
		}
		try {
			if ((value.length() < 3) && (Integer.valueOf(value) < 16)) {
				return true;
			} else
				return false;
		} catch (NumberFormatException e) {
			return false;
		}

	}

	public char getSeparatorFromCSVFile(String filepath) throws IOException {
		BufferedReader buffer = new BufferedReader(new FileReader(filepath));
		String header = buffer.readLine();
		buffer.close();
		if (header.contains(","))
			return ',';
		else if (header.contains(";"))
			return ';';
		else
			return '?';
	}

	public static String[] getFinalHeader() {
		return finalHeader;
	}

	public void initLogger(String filepathlist) {
		FileHandler fh;
		try {
			// This block configure the logger with handler and formatter
			fh = new FileHandler(filepathlist + "_LOG.txt");
			log.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setActualFile(String actualFile) {
		this.actualFile = actualFile;
	}

}