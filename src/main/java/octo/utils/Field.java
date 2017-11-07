package octo.utils;

import lombok.Data;

@Data
public class Field
{
    public static final int TYPE_DATE = 1;
    public static final int TYPE_STRING = 2;
    public static final int TYPE_NUMERIC = 3;
    String columnName;
    int columnLength;
    int columnType;

    /**
     * Used for the header
     * @return
     */
    @Override
    public String toString()
    {
        return columnName;
    }
}
