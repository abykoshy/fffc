package octo;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;

/**
 * Main entry point for the application.
 * <p>
 * <p>Creates the application context and start the REPL.</p>
 *
 * @author Eric Bottard
 */
@SpringBootApplication
public class OctoShell
{

    public static void main(String[] args) throws Exception
    {
        ConfigurableApplicationContext context = SpringApplication.run(OctoShell.class, args);
    }

    @Bean
    public PromptProvider myPromptProvider()
    {
        return () -> new AttributedString("octo-shell:>", AttributedStyle.BOLD.foreground(AttributedStyle.GREEN));
    }
}
