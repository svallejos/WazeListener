package app;

import java.io.File;

public class Main {
	
	private static long tiempo = 300;
	
	public static void main(String[] args) {

		if (args.length > 0) {
			tiempo = Long.parseLong(args[0]);
		}
		
		WazeListener wazeListener = new WazeListener("." + File.separator + "resources", tiempo);
		Thread thread = new Thread(wazeListener);
		thread.start();
	}
}

