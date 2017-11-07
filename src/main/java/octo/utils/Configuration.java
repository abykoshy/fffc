package octo.utils;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Configuration
{

    ArrayList<Field> fields;
    int totalLength;

    public void addField(Field field)
    {
        if (fields == null)
        {
            fields = new ArrayList<Field>();
        }
        fields.add(field);
        totalLength += field.getColumnLength();
    }
}
