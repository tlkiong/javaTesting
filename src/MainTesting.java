import java.util.Hashtable;
import java.util.Scanner;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

public class MainTesting {

	public static void main(String[] args) {
		// testUserSettings();
		// sendEmailTest();
		testMxRecordsLookup();
	}

	private static void testMxRecordsLookup() {
		String[] emailAddressList = { "stone@meekness.com",
				"a-tech@dps.centrin.net.id",
				"trinanda_lestyowati@telkomsel.co.id",
				"asst_dos@astonrasuna.com", "amartabali@dps.centrin.net.id",
				"achatv@cbn.net.id", "bali@tuguhotels.com",
				"baliminimalist@yahoo.com", "bliss@thebale.com",
				"adhidharma@denpasar.wasantara.net.id",
				"kiong90@gmail.com",
				"kiong_90@yahoo.com"};
		
		System.out.println("EmailAddressListLength = "
				+ emailAddressList.length);
		System.out.println("");
		
		for (String add : emailAddressList) {
			String[] fullAddress = add.split("@");
			System.out.println("First part: "+fullAddress[0]);
			System.out.println("Second part: "+fullAddress[1]);
			
			boolean addBool = verifyDomainByCheckingMxRecords(add);
			System.out.println("Address: " + add + "	 Bool: " + addBool);
			System.out.println("");
		}
	}

	// Check MX Records to verify domain
	public static boolean verifyDomainByCheckingMxRecords(
			String emailAddress) {
		
		try {
			String[] temp = emailAddress.split("@");
			// String username = temp[0];
			String domain = temp[1];

			// Common domains referred from
			// http://www.serversmtp.com/en/smtp-settings
			String[] hosts = { "1and1.com", "airmail.net", "aol.com",
					"att.net", "bluewin.ch", "btconnect.tom", "comcast.net",
					"earthlink.net", "gmail.com", "gmx.net", "hotpop.com",
					"libero.it", "lycos.com", "o2.com", "orange.net",
					"live.com", "tin.it", "tiscali.co.uk", "verizon.net",
					"virgin.net", "wanadoo.fr", "yahoo.com" };
			for (String host : hosts) {
				if (domain.trim().equalsIgnoreCase(host)) {
					return true;
				}
			}

			// If here, it is not a common domain
			Record[] records = new Lookup(domain, Type.MX).run();

			if (records != null) {
				return true;
			} else {
				System.out.println("Type MX is null");
				records = new Lookup(domain, Type.A).run();
				if (records == null) {
					System.out.println("Type A is null");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private static void sendEmailTest() {
		Scanner scanner = new Scanner(System.in);

		System.out.print("To: ");
		String recipientEmailAddress = scanner.nextLine();

		/*
		 * String[] recipientEmailAddress = new String[3]; int i =0;
		 * while(scanner.hasNext()){ System.out.print("To: ");
		 * recipientEmailAddress[i] = scanner.nextLine();
		 * 
		 * if(i==2){ break; } i++; }
		 */

		System.out.print("Subject: ");
		String subject = scanner.nextLine();

		/*
		 * System.out.print("Email Template: "); String emailTemplate =
		 * scanner.nextLine();
		 */

		String emailTemplate = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><meta http-equiv=\"Content-Language\" content=\"en-us\" /></head><body><table width=\"100%\" bgcolor=\"#6fa1c6\" name=\"tid\" description=\"mediumBgcolor\"><tr><td><div style=\"padding: 30px; margin: 0px;\"><table width=\"600\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"font-family: 'Verdana';\"><tr><td><table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-bottom: solid 1px #fff;\"><tr><td width=\"10\"><img src=\"c1.gif\" width=\"10\" height=\"25\" /></td><td width=\"580\" bgcolor=\"#FFFFFF\"></td><td width=\"10\"><img src=\"c2.gif\" width=\"10\" height=\"25\" /></td></tr><tr bgcolor=\"#FFFFFF\" ><td width=\"10\" align=\"left\" valign=\"top\" bgcolor=\"#FFFFFF\">&nbsp;</td><td width=\"580\" align=\"left\" valign=\"top\" style=\"padding: 10px 0px 20px 20px;\"><h1 style=\"color: #6994b7; font-family: Georgia, 'Times New Roman', Times, serif; font-weight: normal; letter-spacing:-1px;  font-size:32px; line-height: 32px; padding: 2px 0px; margin: 0px;\" name=\"tid\" description=\"mediumColor\">Company<strong style=\"color:#707f8c; font-weight: normal;\" name=\"tid\" description=\"darkestColor\">Name</strong></h1><span style=\"color:#4b4925; font-size: 10px; font-family: 'Verdana';  padding: 2px 0px; margin: 0px;\"><strong>Type your company slogan here</strong></span></td><td width=\"10\">&nbsp;</td></tr></table><table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td><img src=\"MainImg.jpg\" width=\"600\" /></td></tr></table></td></tr><tr><td bgcolor=\"#FFFFFF\" style=\"padding: 10px 25px;\"><table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#ffffff\"><tr><td colspan=\"2\" valign=\"top\"><p style=\"color:#6b5c48; font-size: 11px; font-weight: normal; line-height: 1.5em; font-family: Verdana, Arial, Helvetica, sans-serif; \">You are subscribed as %%emailaddress%%</p>			</td></tr><tr><td valign=\"top\" style=\"padding: 0px 25px 0px 0px;\"><h1 style=\"color:#678dac; font-family: 'Georgia'; border-bottom: dashed 1px #cccccc; padding: 5px 0px 5px 0px; margin: 0px;  font-size: 22px; font-weight: normal;\" name=\"tid\" description=\"mediumColor\">Content Block Heading</h1><p style=\"color:#333333; font-size: 12px; line-height: 1.5em; font-family: 'Verdana'; \"><img src=\"MainImg1.jpg\" align=\"left\" style=\"margin-right: 15px; margin-bottom: 10px;\" /> This is a block of text for your email campaign. Enter in your content here.<br /><br />Ut stet quodsi habemus has, nec eu atqui altera fabulas, sed kasd aeterno vocibus an. At melius alienum evertitur vis, ei velit ceteros voluptua vix. Sea mentitum neglegentur ex, nostrud adipisci pertinax sea eu. Mea et exerci habemus, duo erat verear electram te. An integre sanctus mentitum sit, mei aliquam inimicus consequat id, sea te essent contentiones. Eu dicta nominati comprehensam sit, nec ex sumo euismod percipit, recteque salutandi delicatissimi quo ea.<br /><br />Lorem ipsum phaedrum assueverit id mea. Eu nemore imperdiet vix, id sed eius meliore omittam, ut his putent efficiantur complectitur.<br /><br />This is a block of text for your email campaign. Enter in your content here.<br /><br />Ut stet quodsi habemus has, nec eu atqui altera fabulas, sed kasd aeterno vocibus an. At melius alienum evertitur vis, ei velit ceteros voluptua vix. Sea mentitum neglegentur ex, nostrud adipisci pertinax sea eu. Mea et exerci habemus, duo erat verear electram te. An integre sanctus mentitum sit, mei aliquam inimicus consequat id, sea te essent contentiones. Eu dicta nominati comprehensam sit, nec ex sumo euismod percipit, recteque salutandi delicatissimi quo ea.This is a block of text for your email campaign. </p><h1 style=\" color:#678dac; font-family: 'Georgia'; border-bottom: dashed 1px #cccccc; padding: 5px 0px 5px 0px; margin: 0px; font-size: 22px; font-weight: normal;\" name=\"tid\" description=\"mediumColor\">Content Block Heading</h1><p style=\"color:#333333; font-size: 12px; line-height: 1.5em; font-family: 'Verdana'; \">This is a block of text for your email campaign. Enter in your content here.<br /><br />Ut stet quodsi habemus has, nec eu atqui altera fabulas, sed kasd aeterno vocibus an. At melius alienum evertitur vis, ei velit ceteros voluptua vix. Sea mentitum neglegentur ex, nostrud adipisci pertinax sea eu. Mea et exerci habemus, duo erat verear electram te. An integre sanctus mentitum sit, mei aliquam inimicus consequat id, sea te essent contentiones. Eu dicta nominati comprehensam sit, nec ex sumo euismod percipit, recteque salutandi delicatissimi quo ea.<br /><br /> Lorem ipsum phaedrum assueverit id mea. Eu nemore imperdiet vix, id sed eius meliore omittam, ut his putent efficiantur complectitur.<br /><br />This is a block of text for your email campaign. Enter in your content here.<br /><br />Ut stet quodsi habemus has, nec eu atqui altera fabulas, sed kasd aeterno vocibus an. At melius alienum evertitur vis, ei velit ceteros voluptua vix. Sea mentitum neglegentur ex, nostrud adipisci pertinax sea eu. Mea et exerci habemus, duo erat verear electram te. An integre sanctus mentitum sit, mei aliquam inimicus consequat id, sea te essent contentiones. Eu dicta nominati comprehensam sit, nec ex sumo euismod percipit, recteque salutandi delicatissimi quo ea.This is a block of text for your email campaign. </p></td><td valign=\"top\" style=\"padding: 15px 15px 10px 0px;\"><h1 style=\"color:#678dac; font-family: 'Georgia'; border-bottom: dashed 1px #cccccc; padding: 0px 0px 5px 0px; margin: 0px;  font-size: 16px; font-weight: normal;\" name=\"tid\" description=\"mediumColor\">Sidebar Block Heading</h1><p style=\"color:#576164; font-size:12px; line-height: 17px;  padding: 5px 0px; margin: 0px; font-family:'Verdana';\">This is the small right column block of text. Here you can place short text items such as event notices, news items and other short notices.</p></td></tr></table></td></tr><tr><td><table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 0px; font-size: 10px; line-height: 1.5em; font-family: 'Verdana'; color:#999;\"><tr bgcolor=\"#FFFFFF\" ><td></td><td style=\"padding:5px 15px 15px 15px;\"><a href=\"%%unsubscribelink%%\" style=\"color: #999\">To stop receiving these emails please unsubscribe.</a> <br />Type your Company Name, Address and Contact Details</td><td></td></tr><tr><td width=\"10\" height=\"25\"><img src=\"c3.gif\" width=\"10\" height=\"25\" /></td><td bgcolor=\"#FFFFFF\" ></td><td width=\"10\" height=\"25\"><img src=\"c4.gif\" width=\"10\" height=\"25\" /></td></tr></table></td></tr></table></div></body></html>";

		EmailSending emailSending = new EmailSending();

		String sendEmailResult = emailSending.sendEmail(recipientEmailAddress,
				subject, emailTemplate);
		System.out.println("Send Email Result: " + sendEmailResult);

		/*
		 * for (String recipientEmail : recipientEmailAddress){
		 * emailSending.sendEmail(recipientEmail, subject, emailTemplate); }
		 */

	}

	private static void testUserSettings() {
		// Testing for UserSettings
		Scanner scanner = new Scanner(System.in);
		int i = 0;
		String key = "";
		String value = "";

		while (i < 3) {
			System.out.print("Key: ");
			key = scanner.nextLine();
			System.out.print("Value: ");
			value = scanner.nextLine();
		}

		/*UserSettings userSettings = new UserSettings();
		userSettings.setPreference(key, value);
		System.out.println("Returned: " + userSettings.getPreference("user"));
		System.out.println("Returned: " + userSettings.getPreference("me"));
		System.out.println("Returned: " + userSettings.getPreference(key));*/

	}
}
