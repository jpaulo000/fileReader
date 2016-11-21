package ufal.ic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by manoel on 18/11/2016.
 */
public class FieldAttributes {
    private String className;
    ///Key - Possible field values, Value - How many times it repeats in the file for this field
    private Map<String, Double> attributes;
    private final String ENDLINE = "\n";

    public FieldAttributes(String field_name){
        //constructor
        className = field_name;
        attributes = new HashMap<>();
    }

    public void addFieldAttributes(String attribute_name){
        if(!attributes.containsKey(attribute_name)){
            attributes.put(attribute_name, 1.0);
        }else{
            attributes.put(attribute_name, attributes.get(attribute_name)+1.0);
        }
    }

    public String printAttributes(){
        Iterator it = attributes.entrySet().iterator();
        String output = ENDLINE+"\t"+className.toUpperCase()+ENDLINE;
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            Double percentToBeTruncated = ((Double)pair.getValue() / Reader.total_num_lines) * 100.0;
            Double percent = BigDecimal.valueOf(percentToBeTruncated).setScale(2, RoundingMode.HALF_UP).doubleValue();

            output += pair.getKey()+" has "+((Double) pair.getValue()).intValue()+" occurrences. [%"
                    +percent+"]"+ENDLINE;
        }
        it.remove();
        System.out.println(output);
        return output;
    }



}
