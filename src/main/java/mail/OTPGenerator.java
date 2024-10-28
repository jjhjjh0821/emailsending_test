package mail;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Base64;

public class OTPGenerator {
    private static final long DISTANCE = 180000 ; // 3 min   //30000; // 30 sec
    private static final String ALGORITHM = "HmacSHA1";
    
    // Genkey로 생성한 Base64로 인코딩된 비밀 키
    private static final byte[] SECRET_KEY = Base64.getDecoder().decode("Ay0IUfHtk8pjAfsd4upQrOis0q4xyy3d3qc3T49X5yA=");
    
    private static long create(long time) throws Exception {
    	byte[] data = new byte[8];

    	long value = time;
    	for (int i = 8; i-- > 0; value >>>= 8) {
    		data[i] = (byte) value;
    	}

    	Mac mac = Mac.getInstance(ALGORITHM);
    	mac.init(new SecretKeySpec(SECRET_KEY, ALGORITHM));

    	byte[] hash = mac.doFinal(data);
    	int offset = hash[20 - 1] & 0xF;

    	long truncatedHash = 0;
    	for (int i = 0; i < 4; ++i) {
    		truncatedHash <<= 8;
    		truncatedHash |= hash[offset + i] & 0xFF;
    	}

    	truncatedHash &= 0x7FFFFFFF;
    	truncatedHash %= 1000000;

    	return truncatedHash;
    }
    
    public static String create() throws Exception {
    	return String.format("%06d", create(new Date().getTime() / DISTANCE));
    }

    public static boolean verify(String code) throws Exception {
    	return create().equals(code);
    }
    
}