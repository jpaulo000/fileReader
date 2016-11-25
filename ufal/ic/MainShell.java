package ufal.ic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.opencsv.CSVReader;

public class MainShell {

	public static void main(String[] args) throws IOException {
		String filepathlist = "R:\\paths.txt";
		String filepath;
		GroupBy groupby = new GroupBy();
		BufferedReader buffer = new BufferedReader(new FileReader(filepathlist));
		
		groupby.initLogger(filepathlist);

		while ((filepath = buffer.readLine()) != null) {

			char separator = groupby.getSeparatorFromCSVFile(filepath);
			CSVReader csv = new CSVReader(new FileReader(filepath), separator);
			groupby.setActualFile(filepath);

			FileWriter writer = new FileWriter(filepath + "_resultados.txt");

			groupby.generateFieldsToAnalyse(csv);
			csv.close();

			csv = new CSVReader(new FileReader(filepath), separator);
			Map<String, ArrayList<Integer>> map = groupby.groupValues(csv, false);

			String eol = System.getProperty("line.separator");
			String[] header = GroupBy.getFinalHeader();

			writer.append("value").append(separator);
			for (int i = 0; i < header.length; i++) {
				if (i != header.length - 1)
					writer.append(header[i]).append(separator);
				else
					writer.append(header[i]).append(eol);
			}

			for (Map.Entry<String, ArrayList<Integer>> entry : map.entrySet()) {
				writer.append(entry.getKey()).append(separator);
				ArrayList<Integer> actualValue = entry.getValue();

				for (int i = 0; i < actualValue.size(); i++) {
					if (i != actualValue.size() - 1)
						writer.append(actualValue.get(i).toString()).append(separator);
					else
						writer.append(actualValue.get(i).toString()).append(eol);
				}
			}
			csv.close();
			writer.close();
		}
		buffer.close();
	}

}
