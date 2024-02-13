package kr.re.keti.sc.ipe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import kr.re.keti.sc.ipe.common.code.Constants;

@SpringBootApplication(scanBasePackages = Constants.BASE_PACKAGE)
public class RunApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunApplication.class, args);
    }
}
