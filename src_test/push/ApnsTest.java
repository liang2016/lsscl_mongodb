package push;

import com.lsscl.app.util.ApnsUtil;

public class ApnsTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String deviceToken = "6191a291 b148466c 1c3dc72f 0b2c9efd fe0c5732 cf729f6d 9d74ffee a8a4b2de";
		String message = "test";
		ApnsUtil.pushNoficationWithSound(deviceToken, null, message);
	}
}
