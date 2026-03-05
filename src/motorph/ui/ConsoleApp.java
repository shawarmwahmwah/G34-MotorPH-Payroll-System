package motorph.ui;

import motorph.util.PathHelper;

public class ConsoleApp {

    public static void main(String[] args) {

        System.out.println("MotorPH Backend Started");

        System.out.println(
            "employees.csv location: "
            + PathHelper.getDataFile("employees.csv").toAbsolutePath()
        );

    }
}