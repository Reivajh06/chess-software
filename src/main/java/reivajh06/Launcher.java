package reivajh06;

import reivajh06.infrastructure.Archchess;
import reivajh06.infrastructure.Configuration;

public class Launcher {

	static void main(String[] args) {
		if(args.length == 1) Configuration.configPath(args[0]);

		Archchess.instance().launch();
	}
}
