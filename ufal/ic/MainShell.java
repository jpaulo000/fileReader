package ufal.ic;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVReader;

public class MainShell {

	public static void main(String[] args) throws IOException {
		String filepath = "R:\\Dados CadUnico\\TB_DOMICILIO_BRASIL.CSV";
		CSVReader csv = new CSVReader(new FileReader(filepath),';');
		GroupBy groupby = new GroupBy();
		FileWriter writer = new FileWriter("R:\\resultados.txt");
		
		csv = new CSVReader(new FileReader(filepath),';');
		String[] header = csv.readNext();
		int columns[] = GroupBy.generateIntegerValues(csv);
		for(int i=0;i<columns.length;i++) System.out.println(header[columns[i]] + " -> " + columns[i]);
		csv.close();
		
		csv = new CSVReader(new FileReader(filepath),';');
		writer.write(groupby.groupValues(columns, csv).toString());
		csv.close();
		writer.close();
	}

}
