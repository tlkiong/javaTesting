package hardware.information;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class Hardware4Win {
	
	public String getWinInfo() {
		String result = "";
		URL url = Hardware4Win.class.getClassLoader().getResource("Resources/getHardwareInformation.bat");
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime
					.exec("powershell.exe  \""+url.getPath().toString().substring(1)+"\"  ");
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
