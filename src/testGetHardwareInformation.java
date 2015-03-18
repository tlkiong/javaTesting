import hardware.information.Hardware4Mac;
import hardware.information.Hardware4Nix;
import hardware.information.Hardware4Win;


public class testGetHardwareInformation {

	public static void main(String[] args) {
		//String serialNumber = Hardware4Mac.getSerialNumber();
		String serialNumber = Hardware4Win.getSerialNumber();
		//String serialNumber = Hardware4Nix.getSerialNumber();
		
		System.out.println("Serial Number: "+serialNumber);
	}

}
