package hardware.information;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Hardware4Win {
	public String getWinInfo() {
		String result = "";

		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime
					.exec("powershell.exe  \"C:\\Users\\User\\Desktop\\getHardwareInformation.bat\"  ");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);
			String line = "";
			while ((line = reader.readLine()) != null) {
				result += line + "||";
				System.out.println("result: " + result);
			}
			reader.close();
			proc.getOutputStream().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.trim();
	}
}
