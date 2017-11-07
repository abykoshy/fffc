package octo.utils;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to check the conversion of a fixed width file to a CSV
 *
 * @author aby.koshy
 */
public class ConvertToCsvTest
{
    @Test
    public void validate_metadata_config() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        convertToCsv.setConfigurationFile("src/test/resources/config.txt");
        assertThat(convertToCsv.getConfiguration().getFields()).hasSize(4);
        assertThat(convertToCsv.getConfiguration().getFields().get(0)).hasFieldOrPropertyWithValue("columnName","Birth date");
        assertThat(convertToCsv.getConfiguration().getFields().get(0)).hasFieldOrPropertyWithValue("columnLength",10);
        assertThat(convertToCsv.getConfiguration().getFields().get(0)).hasFieldOrPropertyWithValue("columnType",1);
        assertThat(convertToCsv.getConfiguration().getFields().get(1)).hasFieldOrPropertyWithValue("columnName","First name");
        assertThat(convertToCsv.getConfiguration().getFields().get(2)).hasFieldOrPropertyWithValue("columnName","Last name");
        assertThat(convertToCsv.getConfiguration().getFields().get(3)).hasFieldOrPropertyWithValue("columnName","Weight");
    }

    @Test(expected = ConfigurationException.class)
    public void validate_metadata_config_invalid() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        convertToCsv.setConfigurationFile("src/test/resources/config_invalid.txt");
    }

    @Test
    public void validate_data_line_date() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        convertToCsv.setConfigurationFile("src/test/resources/config.txt");
        try
        {
            assertThat(convertToCsv.getLineAsList("190-01-01John           Smith           81.5", 1)).isNotEmpty();
        }
        catch (DataValidationException e)
        {
            assertThat(e.getMessage()).contains("Date format");
        }
    }

    @Test
    public void validate_data_line_string_escape() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        convertToCsv.setConfigurationFile("src/test/resources/config.txt");
        List<String> record = convertToCsv.getLineAsList("1990-01-01Jo,hn          Smith           81.5", 1);
        assertThat(record).isNotEmpty();
        assertThat(record.get(1)).isEqualTo("\"Jo,hn\"");
    }

    @Test
    public void validate_data_line_numeric() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        convertToCsv.setConfigurationFile("src/test/resources/config.txt");
        try
        {
            assertThat(convertToCsv.getLineAsList("1900-01-01John           Smith     ", 1)).isNotEmpty();
        }
        catch (DataValidationException e)
        {
            assertThat(e.getMessage()).contains("Numeric format");
        }
    }

    @Test
    public void test_conversion() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        convertToCsv.setConfigurationFile("src/test/resources/config.txt");
        int sourceFileLines = FileUtils.readLines(new File("src/test/resources/input.txt"), Charset.defaultCharset()).size();
        convertToCsv.convertFixedWidthFileToCsv("src/test/resources/input.txt", "output.csv");
        assertThat(new File("output.csv")).exists();
        assertThat(FileUtils.readLines(new File("output.csv"), Charset.defaultCharset())).hasSize(sourceFileLines + 1);
    }

    @Test(expected = ConversionException.class)
    public void test_conversion_input_file_does_not_exist() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        convertToCsv.setConfigurationFile("src/test/resources/config.txt");
        convertToCsv.convertFixedWidthFileToCsv("src/test/resources/inputdd.txt", "output.csv");
    }

    @Test
    public void test_conversion_input_file_with_errors() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        convertToCsv.setConfigurationFile("src/test/resources/config.txt");
        try
        {
            convertToCsv.convertFixedWidthFileToCsv("src/test/resources/inputwitherror.txt", "output.csv");
        }
        catch (ConversionException e)
        {
            assertThat(e.getMessage()).endsWith("line #12");
        }
    }

    @Test
    public void test_conversion_when_not_configured() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        try
        {
            convertToCsv.convertFixedWidthFileToCsv("src/test/resources/inputwitherror.txt", "output.csv");
        }
        catch (ConfigurationException e)
        {
            assertThat(e.getMessage()).startsWith("The configuration has not been setup");
        }
    }

    @Test
    @Ignore
    public void test_conversion_large_file() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        convertToCsv.setConfigurationFile("src/test/resources/config.txt");
        convertToCsv.convertFixedWidthFileToCsv("src/test/resources/inputlarge.txt", "output.csv");
        assertThat(new File("output.csv")).exists();
    }

    @Test
    public void test_conversion_encoding() throws Exception
    {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        convertToCsv.setConfigurationFile("src/test/resources/config.txt");
        convertToCsv.convertFixedWidthFileToCsv("src/test/resources/input_encoding.txt", "output.csv");
        assertThat(new File("output.csv")).exists();
        List<String> lines = FileUtils.readLines(new File("output.csv"), StandardCharsets.UTF_8);

        assertThat(lines.get(1)).contains("你好");
    }
}
