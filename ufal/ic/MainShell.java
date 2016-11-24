package ufal.ic;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.opencsv.CSVReader;

public class MainShell {

	public static void main(String[] args) throws IOException {
		String filepath = "R:\\Dados CadUnico\\TB_DOMICILIO_BRASIL.CSV";
		CSVReader csv = new CSVReader(new FileReader(filepath),';');
		GroupBy groupby = new GroupBy();
		FileWriter writer = new FileWriter("R:\\resultados.txt");
		
		csv = new CSVReader(new FileReader(filepath),';');
		String[] header = csv.readNext();
		int columns[] = GroupBy.generateColumnsValues(csv);
		for(int i=0;i<columns.length;i++) System.out.println(header[columns[i]] + " -> " + columns[i]);
		csv.close();
		
		csv = new CSVReader(new FileReader(filepath),';');
		Map<String, ArrayList<Integer>> map = groupby.groupValues(columns, csv,true);
		
		String eol = System.getProperty("line.separator");
		for (Map.Entry<String, ArrayList<Integer>> entry : map.entrySet()) {
		    writer.append(entry.getKey())
		          .append(',')
		          .append(entry.getValue().toString())
		          .append(eol);
		  }
		
		csv.close();
		writer.close();
	}

}
