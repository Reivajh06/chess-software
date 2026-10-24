package reivajh06;

import reivajh06.api.ChessAPI;

import java.util.Arrays;

public class Launcher {

	static void main() {
		new ChessAPI().start();
		System.out.println(Arrays.toString("4k3/8/8/8/8/8/3r4/4K3 w - - 0 1".split(" ", 5)));
	}
}
