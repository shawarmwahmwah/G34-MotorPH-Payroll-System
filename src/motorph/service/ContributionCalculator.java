package motorph.service;

import java.io.BufferedReader;
import java.io.FileReader;
 
public class ContributionCalculator {

    // File locations
    private static final String SSS_FILE = "data/sss_contribution.csv";
    private static final String PHILHEALTH_FILE = "data/philhealth_contribution.csv";
    private static final String PAGIBIG_FILE = "data/pagibig_contribution.csv";
    private static final String TAX_FILE = "data/withholding_tax.csv";

    public double computeSSS(double grossPay) {

        try (BufferedReader br = new BufferedReader(new FileReader(SSS_FILE))) {

            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {

                String[] p = line.split(",");

                double min = Double.parseDouble(p[0]);
                double max = Double.parseDouble(p[1]);
                double employeeShare = Double.parseDouble(p[2]);

                if (grossPay >= min && grossPay <= max) {
                    return employeeShare;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /*
     * Compute PhilHealth contribution.
     * Most tables use percentage of salary.
     */
    public double computePhilHealth(double grossPay) {

        try (BufferedReader br = new BufferedReader(new FileReader(PHILHEALTH_FILE))) {

            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {

                String[] p = line.split(",");

                double min = Double.parseDouble(p[0]);
                double max = Double.parseDouble(p[1]);
                double rate = Double.parseDouble(p[2]);

                if (grossPay >= min && grossPay <= max) {

                    double totalContribution = grossPay * rate;

                    // employee pays half
                    return totalContribution / 2;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /*
     * Compute Pag-IBIG contribution
     */
    public double computePagibig(double grossPay) {

        try (BufferedReader br = new BufferedReader(new FileReader(PAGIBIG_FILE))) {

            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {

                String[] p = line.split(",");

                double min = Double.parseDouble(p[0]);
                double max = Double.parseDouble(p[1]);
                double rate = Double.parseDouble(p[2]);

                if (grossPay >= min && grossPay <= max) {
                    return grossPay * rate;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /*
     * Compute withholding tax
     */
    public double computeWithholdingTax(double taxableIncome) {

        try (BufferedReader br = new BufferedReader(new FileReader(TAX_FILE))) {

            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {

                String[] p = line.split(",");

                double min = Double.parseDouble(p[0]);
                double max = Double.parseDouble(p[1]);
                double baseTax = Double.parseDouble(p[2]);
                double rate = Double.parseDouble(p[3]);

                if (taxableIncome >= min && taxableIncome <= max) {

                    double excess = taxableIncome - min;

                    return baseTax + (excess * rate);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}