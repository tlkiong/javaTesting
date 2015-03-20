import hardware.information.Hardware4Win;
import hardware.information.OsValidator;
import hardware.information.OsValidator.OSTYPE;

public class TestGetHardwareInformation {

	public static void main(String[] args) {
		String whatOS = OsValidator.whichOs();
		String serialNumber = "";
		if (whatOS.equals(OsValidator.OSTYPE.WINDOWS.toString())) {
			System.out.println("Windows - Your OS is: " + whatOS);
			serialNumber = getWindowsId(30, 60);
		} else if (whatOS.equals(OsValidator.OSTYPE.MAC.toString())) {
			System.out.println("Mac - Your OS is: " + whatOS);
		} else if (whatOS.equals(OsValidator.OSTYPE.UNIX.toString())) {
			System.out.println("Unix - Your OS is: " + whatOS);
		} else {
			System.out.println("Your OS is: " + whatOS);
		}
		System.out.println("Serial Number: " + serialNumber);
	}

	/**
	 * Get both motherboard SN and hard drive SN
	 * 
	 * @return motherboardSN || harddriveSN
	 */
	public static String getWindowsId(int startIndex, int endIndex) {
		String hardwareID = "";
		Hardware4Win hardware4Win = new Hardware4Win();
		TestGetHardwareInformation testGetHardwareInformation = new TestGetHardwareInformation();
		String encodedWinInformation = testGetHardwareInformation
				.encodeData(hardware4Win.getWinInfo());

		hardwareID = encodedWinInformation.substring(startIndex, endIndex);

		return hardwareID;
	}

	/**
	 * Encode Data using LZString
	 * 
	 * @param data
	 * @return the encodedData
	 */
	public String encodeData(String data) {
		String toBeEncoded = data;
		String encodedValue = LZString.compressToBase64(toBeEncoded);
		// '$' is group symbol in regex's replacement parameter
		String newEncodedValue = encodedValue.replaceAll("=", "\\$");
		String correctEncodedValue = newEncodedValue.replaceAll("/", "-");
		return correctEncodedValue;
	}

}
