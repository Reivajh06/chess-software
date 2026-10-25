package reivajh06.chess.model;

public enum File {

	A(0x61),
	B(0x62),
	C(0x63),
	D(0x64),
	E(0x65),
	F(0x66),
	G(0x67),
	H(0x68);

	public final int code;

	File(int code) {
		this.code = code;
	}

	public static File next(File currentFile) {
		if(currentFile.equals(File.H)) return null;
		return File.values()[currentFile.code - File.A.code + 1];
	}

	public static File previous(File currentFile) {
		if(currentFile.equals(File.A)) return null;
		return File.values()[currentFile.code - File.A.code - 1];
	}
}
