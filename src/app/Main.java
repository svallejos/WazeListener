package app;
public class Main {
	public static void main(String[] args) {

		long tiempo = 300;
		if (args.length > 0) {
			tiempo = Long.parseLong(args[0]);
		}
		WazeListener wl = new WazeListener(".\\resources", tiempo);
		Thread t = new Thread(wl);
		t.start();
	}
}

