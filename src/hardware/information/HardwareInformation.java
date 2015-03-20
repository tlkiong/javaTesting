package hardware.information;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import mainTest.LZString;
import mainTest.TestGetHardwareInformation;

public class HardwareInformation {
	
	public String getHardwareInformation(){
		String whatOS = OsValidator.whichOs();
		String hardwareId= "";
		if (whatOS.equals(OsValidator.OSTYPE.WINDOWS.toString())) {
			System.out.println("Windows - Your OS is: " + whatOS);
			hardwareId = getWinInfo();
		} else if (whatOS.equals(OsValidator.OSTYPE.MAC.toString())) {
			System.out.println("Mac - Your OS is: " + whatOS);
			hardwareId = getMacInfo();
		} else if (whatOS.equals(OsValidator.OSTYPE.UNIX.toString())) {
			System.out.println("Unix - Your OS is: " + whatOS);
			hardwareId = getUnixInfo();
		} else {
			System.out.println("Your OS is: " + whatOS);
		}
		
		String encodedHardwareId = encodedId(30,60,hardwareId);
		
		return encodedHardwareId;
	}
	
	/**
	 * Encode Data using LZString
	 * 
	 * @param data
	 * @return the encodedData
	 */
	public static String encodeData(String data) {
		String toBeEncoded = data;
		String encodedValue = LZString.compressToBase64(toBeEncoded);
		// '$' is group symbol in regex's replacement parameter
		String newEncodedValue = encodedValue.replaceAll("=", "\\$");
		String correctEncodedValue = newEncodedValue.replaceAll("/", "-");
		return correctEncodedValue;
	}
	
	/**
	 * Encode the Hardware ID
	 * 
	 * @param startIndex - Start Index of the substring
	 * @param endIndex - End Index of the substring
	 * @param hardwareID - Original Hardware ID
	 * @return encoded Hardware ID with length of endIndex-startIndex
	 */
	public static String encodedId(int startIndex, int endIndex, String hardwareID) {
		return encodeData(hardwareID).substring(startIndex, endIndex);
	}
	
	/**
	 * Getting hardware information for windows OS
	 * @return
	 */
	public static String getWinInfo() {
		String hardwareId = "";
		URL url = HardwareInformation.class.getClassLoader().getResource("Resources/getHardwareInformation.bat");
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime
					.exec("powershell.exe  \""+url.getPath().toString().substring(1)+"\"  ");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);
			String line = "";
			while ((line = reader.readLine()) != null) {
				hardwareId += line + "||";
				//System.out.println("result: " + result);
			}
			reader.close();
			proc.getOutputStream().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hardwareId.trim();
	}
	

	private String getUnixInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	private String getMacInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
