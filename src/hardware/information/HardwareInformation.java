package hardware.information;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import mainTest.LZString;
import mainTest.TestGetHardwareInformation;

public class HardwareInformation {

	/**
	 * Get Hardware Informations including OS
	 * 
	 * @return the hardwareID which is encoded
	 */
	public String getHardwareInformation() {
		String whatOS = OsValidator.whichOs();
		String hardwareId = "";
		String encodedHardwareId = "";
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

		if (hardwareId != null && !hardwareId.isEmpty()) {
			encodedHardwareId = encodedId(0, hardwareId.length(), hardwareId);
		} else {
			encodedHardwareId = "";
		}
		return encodedHardwareId;
	}
	
	/**
	 * Get Hardware Informations including OS with specific length
	 * 
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	public String getHardwareInformation(int startIndex, int endIndex) {
		String whatOS = OsValidator.whichOs();
		String hardwareId = "";
		String encodedHardwareId = "";
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

		if (hardwareId != null && !hardwareId.isEmpty()) {
			encodedHardwareId = encodedId(startIndex, endIndex, hardwareId);
		} else {
			encodedHardwareId = "";
		}
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
	 * @param startIndex
	 *            - Start Index of the substring
	 * @param endIndex
	 *            - End Index of the substring
	 * @param hardwareID
	 *            - Original Hardware ID
	 * @return encoded Hardware ID with length of endIndex-startIndex
	 */
	public static String encodedId(int startIndex, int endIndex,
			String hardwareID) {
		return encodeData(hardwareID).substring(startIndex, endIndex);
	}

	/**
	 * Getting hardware information for windows OS
	 * 
	 * @return
	 */
	public static String getWinInfo() {
		String hardwareId = "";
		URL url = HardwareInformation.class.getClassLoader().getResource(
				"Resources/getHardwareInformation.bat");
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec("powershell.exe  \""
					+ url.getPath().toString().substring(1) + "\"  ");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);
			String line = "";
			while ((line = reader.readLine()) != null) {
				hardwareId += line + "||";
				// System.out.println("hardwareId: " + hardwareId);
			}
			reader.close();
			proc.getOutputStream().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hardwareId.trim();
	}

	/**
	 * Getting hardware information for Unix information
	 * 
	 * @return
	 */
	private String getUnixInfo() {
		String hardwareId = "";
		try {
			Process proc = Runtime.getRuntime().exec("/bin/bash -c lspci");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);
			String line = "";
			int index = 0;
			while ((line = reader.readLine()) != null) {
				index = line.lastIndexOf("(rev");
				if (index != -1) {
					hardwareId += line.substring((line.lastIndexOf(":") + 2),
							index).trim() + "||";
				} else {
					hardwareId += line.substring((line.lastIndexOf(":") + 2)).trim()
							+ "||";
				}
				//System.out.println("hardwareId: " + hardwareId);
			}
			reader.close();
			proc.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hardwareId;
	}

	private String getMacInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
