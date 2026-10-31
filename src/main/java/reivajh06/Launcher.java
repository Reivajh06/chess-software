package reivajh06;

import java.io.File;

public class Launcher {

	static void main(String[] args) {
		int port = args.length == 0 ? 5000 : Integer.parseInt(args[0]);
		File homeFolder = args.length == 0 ? new File("temp") : new File(args[1]);

		Archchess.instance()
				.home(homeFolder)
				.port(port)
				.launch();
	}
}
