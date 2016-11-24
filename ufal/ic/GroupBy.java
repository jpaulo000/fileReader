package ufal.ic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.opencsv.CSVReader;

public class GroupBy {

	private static ArrayList<String> valuesList = new ArrayList<String>();
	private static int headerLength;
	private static int oldHeaderLength;

	public Map<String, ArrayList<Integer>> groupValues(int[] columns, CSVReader csv, Boolean isInteger) throws IOException {

		String[] header = csv.readNext();
		headerLength = header.length;
		oldHeaderLength = headerLength;

		header = setRow(header, columns);
		headerLength = header.length;

		for (int i = 0; i < headerLength; i++)
			System.out.println(header[i]);

		String[] oldRow;
		int rowcount = 0;

		if(isInteger) generateIntegersValues();

		Map<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();

		while ((oldRow = csv.readNext()) != null) {

			String[] newRow = setRow(oldRow, columns);

			if(!isInteger) getUniqueValues(newRow);

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
			if (rowcount % 500000 == 0) {
				System.out.println("Lida linha: " + rowcount);
				System.out.println(map);
			}
			rowcount++;
		}
		valuesList.clear();
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

	public void generateIntegersValues(){
		for(int i=0;i<17;i++) valuesList.add(Integer.toString(i));
		valuesList.add("");
	}
	
	public static int[] generateColumnsValues(CSVReader csv) throws IOException {
		int rowcount = 0;
		ArrayList<Integer> sumvalor = new ArrayList<Integer>();

		String[] header = csv.readNext();
		int intHeaderLength = header.length;

		for (int i = 0; i < intHeaderLength; i++)
			sumvalor.add(0);

		while (rowcount != 100000) {
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
		System.out.println("media: " + average);

		ArrayList<Integer> columns = new ArrayList<Integer>();

		for (int i = 0; i < sumvalor.size(); i++)
			if (sumvalor.get(i) > average+(average/2))
				columns.add(i);

		return columns.stream().mapToInt(i -> i).toArray();
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
			if ((value.length() < 3) && (Integer.valueOf(value) < 16)){
				return true;}
			else
				return false;
		} catch (NumberFormatException e) {
			return false;
		}

	}

}
