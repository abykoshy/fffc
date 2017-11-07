package octo.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Data
@Slf4j
public class ConvertToCsv
{
    Configuration configuration;

    public ConvertToCsv()
    {
        configuration = new Configuration();
    }

    /**
     * Set a configuration file which contains the following
     * Birth date,10,date
     * First name,15,string
     * Last name,15,string
     * Weight,5,numeric
     *
     * @param fileName The configuration file
     * @throws ConfigurationException if the format of the configuration file is wrong
     */
    public void setConfigurationFile(String fileName) throws ConfigurationException
    {
        Scanner scanner = null;
        try
        {
            scanner = new Scanner(new File(fileName), "UTF-8");
            String line;
            while (scanner.hasNext())
            {
                line = scanner.nextLine();
                validateConfigLine(line);

            }
        }
        catch (FileNotFoundException e)
        {
            throw new ConfigurationException("The configuration file does not exist");
        }
        finally
        {
            if (scanner != null)
            {
                scanner.close();
            }
        }
    }

    private void validateConfigLine(String line) throws ConfigurationException
    {
        String[] configLine = StringUtils.split(line, ",");
        if (configLine == null || configLine.length != 3)
        {
            throw new ConfigurationException("The configuration file is not setup correctly : the format is <column label>,<columnwidth>,<columntype>, e.g Birth date,10,date\nFirst name,15,string\nWeight,5,numeric");
        } else
        {
            if (getTypeInt(configLine[2]) == 0)
            {
                throw new ConfigurationException("Data type can only be 'data' or 'string' or 'numeric'");
            } else
            {
                Field field = new Field();
                field.setColumnName(configLine[0]);
                field.setColumnLength(Integer.parseInt(configLine[1]));
                field.setColumnType(getTypeInt(configLine[2]));
                configuration.addField(field);
            }
        }
    }

    private void writeToCsv(List<String> record, BufferedWriter bufferedWriter) throws IOException
    {
        bufferedWriter.write(StringUtils.join(record, ","));
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private int getTypeInt(String dataTypeString)
    {
        if (StringUtils.equals(dataTypeString, "date"))
        {
            return Field.TYPE_DATE;
        } else if (StringUtils.equals(dataTypeString, "string"))
        {
            return Field.TYPE_STRING;
        } else if (StringUtils.equals(dataTypeString, "numeric"))
        {
            return Field.TYPE_NUMERIC;
        }
        return 0;
    }

    /**
     * Convert a source file in fixed width format to a CSV file
     *
     * @param sourceFileName Source file name
     * @param outputFileName Output file name
     * @throws ConversionException
     */
    public void convertFixedWidthFileToCsv(String sourceFileName, String outputFileName) throws ConversionException, ConfigurationException
    {
        if (CollectionUtils.isEmpty(configuration.getFields()))
        {
            throw new ConfigurationException("The configuration has not been setup using the setConfigurationFile() method");
        }
        BufferedWriter bufferedWriter = null;
        try
        {
            Scanner scanner = new Scanner(new FileInputStream(sourceFileName), StandardCharsets.UTF_8.name());
            FileUtils.deleteQuietly(new File(outputFileName));
            String line;
            File outputFile = new File(outputFileName);
            outputFile.createNewFile();
            bufferedWriter = Files.newBufferedWriter(Paths.get(outputFileName), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            bufferedWriter.write(StringUtils.join(configuration.getFields(), ","));
            bufferedWriter.newLine();
            int lineNumber = 1;
            while (scanner.hasNext())
            {
                line = scanner.nextLine();
                writeToCsv(getLineAsList(line, lineNumber++), bufferedWriter);
            }

            bufferedWriter.flush();
        }
        catch (IOException | DataValidationException e)
        {
            throw new ConversionException(e.getMessage());
        }
        finally
        {
            try
            {
                if (bufferedWriter != null)
                {
                    bufferedWriter.close();
                }
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
            }
        }

    }

    /**
     * Validates the input line with the configuration and throws {@link DataValidationException}
     * @param line The input line
     * @param lineNumber The line number to be used in throwing an exception
     * @return
     * @throws DataValidationException
     */
    public List<String> getLineAsList(String line, int lineNumber) throws DataValidationException
    {
        ArrayList<String> record = new ArrayList<>();
        if (StringUtils.isNotEmpty(line) && CollectionUtils.isNotEmpty(getConfiguration().getFields()))
        {
            line = StringUtils.rightPad(line, configuration.totalLength);
            for (Field field : configuration.getFields())
            {
                if (field.getColumnLength() <= line.length())
                {
                    String fieldValue = line.substring(0, field.getColumnLength());
                    SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat targetDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    if (field.getColumnType() == Field.TYPE_DATE)
                    {
                        try
                        {
                            DateUtils.parseDate(fieldValue, new String[]{"yyyy-MM-dd"});
                            fieldValue = targetDateFormat.format(sourceDateFormat.parse(fieldValue));
                        }
                        catch (ParseException e)
                        {
                            throw new DataValidationException("Date format of source has to be yyyy-mm-dd at line #" + lineNumber);
                        }
                    } else if (field.getColumnType() == Field.TYPE_NUMERIC)
                    {
                        try
                        {
                            Float.parseFloat(fieldValue);
                        }
                        catch (NumberFormatException e)
                        {
                            throw new DataValidationException("Numeric format has to be a number which can have a decimal at line #" + lineNumber);
                        }
                    }
                    line = line.substring(field.getColumnLength());
                    record.add(StringEscapeUtils.escapeCsv(fieldValue.trim()));
                }
            }
            if (record.size() != configuration.getFields().size())
            {
                throw new DataValidationException("The line does not contain the configured set of columns at line #" + lineNumber);
            }
        }
        return record;
    }
}