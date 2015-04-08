package mainTest;

import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

public class CountdownTimer {
	Toolkit toolkit;

	Timer timer;

	public CountdownTimer(int seconds) {
		toolkit = Toolkit.getDefaultToolkit();
		timer = new Timer();
		timer.schedule(new RemindTask(), 1, // initial delay
				seconds * 1000); // subsequent rate
	}

	class RemindTask extends TimerTask {
		int numWarningBeeps = 3;

		public void run() {
			if (numWarningBeeps > 0) {
				toolkit.beep();
				System.out.println("Beep!");
				// numWarningBeeps--;
			} else {
				toolkit.beep();
				System.out.println("Time's up!");
				// timer.cancel(); //Not necessary because we call System.exit
				System.exit(0); // Stops the AWT thread (and everything else)
			}
		}
	}

	public static void main(String args[]) {
		System.out.println("About to schedule task.");
		new CountdownTimer(1);
		System.out.println("Task scheduled.");
	}
}
