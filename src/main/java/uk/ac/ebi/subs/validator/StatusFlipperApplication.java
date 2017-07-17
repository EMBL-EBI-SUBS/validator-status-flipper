package uk.ac.ebi.subs.validator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("uk.ac.ebi.subs.validator")
public class StatusFlipperApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(StatusFlipperApplication.class);
        ApplicationPidFileWriter applicationPidFileWriter = new ApplicationPidFileWriter();
        springApplication.addListeners(applicationPidFileWriter);
        springApplication.run(args);
    }
}