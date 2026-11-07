package reivajh06.archchess;

import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.Configuration;

public class Launcher {

	static void main(String[] args) {
		if(args.length == 1) Configuration.configPath(args[0]);

		Archchess.instance().launch();
	}
}
