import java.io.BufferedReader;
import java.io.InputStreamReader;

public class InspectMW {
    public static void main(String[] args) {
        System.out.println("=== Testing Runtime.exec('wm overscan') ===");
        try {
            Process p = Runtime.getRuntime().exec("wm overscan 100,50,100,60");
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println("stderr: " + line);
            }
            int code = p.waitFor();
            System.out.println("Exit code: " + code);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
