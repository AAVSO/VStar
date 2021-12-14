public class JavaOsArch {
    public static void main(String[] args) {
	String osArch = System.getProperty("os.arch");
        System.out.println(osArch);
	if ("amd64".equals(osArch) || "ia64".equals(osArch)) {
		System.exit(0);
	} else {
		System.exit(1);	
	}
    }
}
