package hardware.information;

public class OsValidator {
	private static String myOS = System.getProperty("os.name").toLowerCase();
	public enum OSTYPE {
		WINDOWS, MAC, UNIX
	}
	public static String whichOs(){
		String operatingSystem = "";
		if (isWindows()) {
			operatingSystem = OSTYPE.WINDOWS.toString();
		} else if (isMac()) {
			operatingSystem = OSTYPE.MAC.toString();
		} else if (isUnix()) {
			operatingSystem = OSTYPE.UNIX.toString();
		} else {
			operatingSystem = myOS;
		}
		return operatingSystem;
	}
	
	public static boolean isWindows() {
		return (myOS.indexOf("win") >= 0);
	}
 
	public static boolean isMac() {
		return (myOS.indexOf("mac") >= 0);
	}
 
	public static boolean isUnix() {
		return (myOS.indexOf("nix") >= 0 || myOS.indexOf("nux") >= 0 || myOS.indexOf("aix") > 0 );
	}
}
