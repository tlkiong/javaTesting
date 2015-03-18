import hardware.information.Hardware4Win;


public class testGetHardwareInformation {

	public static void main(String[] args) {
		/**
		 * Need to detect OS and then execute the correct one.
		 */
		//String serialNumber = Hardware4Mac.getSerialNumber();
		//String serialNumber = Hardware4Win.getSerialNumber();
		String serialNumber = getBothValue();
		//String serialNumber = Hardware4Nix.getSerialNumber();
		
		System.out.println("Serial Number: "+serialNumber);
	}
	
	/**
	 * Get both motherboard SN and hard drive SN
	 * @return motherboardSN || harddriveSN
	 */
	public static String getBothValue() {
		String bothSN = "";

		String motherboardSN = Hardware4Win.getMotherboardSN();
		String hdSN = Hardware4Win.getHardDriveSN("C");

		bothSN = motherboardSN + " || " + hdSN;

		return bothSN;
	}

}
