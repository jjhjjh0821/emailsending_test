package mail;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Base64;

public class OTPGenerator {
	
	// OTP 생성 간격을 설정 (3분 단위로 OTP가 변경됨) - 30000 = 30초
    private static final long DISTANCE = 180000 ; // 3분
    private static final String ALGORITHM = "HmacSHA1"; // 해시 알고리즘으로 HmacSHA1 사용
    
    // Genkey로 생성한 Base64로 인코딩된 비밀 키 (사용자 정의 필요)
    //private static final byte[] SECRET_KEY = "define your secret key here".getBytes();
    private static final byte[] SECRET_KEY = Base64.getDecoder().decode("Ay0IUfHtk8pjAfsd4upQrOis0q4xyy3d3qc3T49X5yA=");
    
    // OTP 생성 메서드 (시간을 기반으로 OTP 생성)
    private static long create(long time) throws Exception {
    	
    	byte[] data = new byte[8]; // 시간 데이터를 담을 배열

    	// 시간을 8바이트 배열로 변환
    	long value = time;
    	for (int i = 8; i-- > 0; value >>>= 8) {
    		data[i] = (byte) value;
    	}

    	// HmacSHA1 알고리즘을 사용하여 해시 생성
    	Mac mac = Mac.getInstance(ALGORITHM);
    	mac.init(new SecretKeySpec(SECRET_KEY, ALGORITHM)); // 비밀 키로 초기화

    	byte[] hash = mac.doFinal(data); // 해시 결과 생성
    	int offset = hash[20 - 1] & 0xF; // 해시의 특정 오프셋을 계산

    	// 해시 값을 숫자로 변환하여 6자리의 OTP 생성
    	long truncatedHash = 0;
    	for (int i = 0; i < 4; ++i) {
    		truncatedHash <<= 8;
    		truncatedHash |= hash[offset + i] & 0xFF;
    	}

    	truncatedHash &= 0x7FFFFFFF; // 최종 해시 값을 제한된 범위로 변환
    	truncatedHash %= 1000000; // 6자리로 제한

    	return truncatedHash;
    }
    
    // OTP 생성 메서드
    public static String create() throws Exception {
    	return String.format("%06d", create(new Date().getTime() / DISTANCE));
    }

    // OTP 검증 메서드 (생성된 OTP와 입력된 OTP 비교)
    public static boolean verify(String code) throws Exception {
    	return create().equals(code);
    }
    
}