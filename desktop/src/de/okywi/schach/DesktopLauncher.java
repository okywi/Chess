package de.okywi.schach;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import de.okywi.schach.Schach;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.useVsync(true);
		config.setTitle("Schach");
		config.setResizable(false);

		config.setWindowedMode(Schach.BOARD_SIZE, Schach.BOARD_SIZE);

		new Lwjgl3Application(new Schach(), config);
	}
}
