package octo;

import lombok.extern.slf4j.Slf4j;
import octo.utils.ConfigurationException;
import octo.utils.ConversionException;
import octo.utils.ConvertToCsv;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@Slf4j
@ShellComponent
public class OctoCommands
{
    @ShellMethod("Convert a Fixed width file to a CSV")
    public String converttocsv(
            @ShellOption(help="Configuration file containing <column label>,<columnwidth>,<columntype>, e.g \nBirth date,10,date\nFirst name,15,string\nWeight,5,numeric") String configFileName,
            @ShellOption(help="Input fixed width format file") String inputFileName,
            @ShellOption(help="Output csv filename", defaultValue = "output.csv") String outputFileName
    ) {
        ConvertToCsv convertToCsv = new ConvertToCsv();
        try
        {
            convertToCsv.setConfigurationFile(configFileName);
            convertToCsv.convertFixedWidthFileToCsv(inputFileName, outputFileName);
        }
        catch (ConfigurationException e)
        {
            log.error(e.getMessage());
        }
        catch (ConversionException e)
        {
            log.error(e.getMessage());
        }
        return "OK";
    }
}


