package mainTest;

import hardware.information.HardwareInformation;

public class TestGetHardwareInformation {

	public static void main(String[] args) {
		HardwareInformation hardwareInformation = new HardwareInformation();
		String hardwareId = hardwareInformation.getHardwareInformation(30, 60);
		//String hardwareId = hardwareInformation.getHardwareInformation();

		System.out.println("Hardware ID: " + hardwareId);
	}
}
