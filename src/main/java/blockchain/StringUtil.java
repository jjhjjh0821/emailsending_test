package blockchain;

import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

// 블록체인에서 암호화 및 서명을 처리하는 여러 유틸리티 메서드를 제공
public class StringUtil {
	
	// 입력받은 문자열을 SHA-256 알고리즘을 사용하여 해시 값으로 변환
	public static String applySha256(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");  // SHA-256 알고리즘을 사용
			byte[] hash = digest.digest(input.getBytes("UTF-8")); // 입력된 문자열을 바이트 배열로 변환하여 해싱
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) { 
				String hex = Integer.toHexString(0xff & hash[i]); // 바이트를 16진수로 변환
				if (hex.length() == 1)
					hexString.append('0'); // 1자리 수일 경우 앞에 0 추가
				hexString.append(hex);
			}
			return hexString.toString(); // 최종 해시 문자열 반환
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ECDSA(Elliptic Curve Digital Signature Algorithm)를 사용하여 개인 키로 서명 & 바이트 배열로 결과 반환
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
		Signature dsa;
		byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC"); // ECDSA 알고리즘 사용
			dsa.initSign(privateKey); // 개인 키로 서명 초기화
			byte[] strByte = input.getBytes(); // 입력 문자열을 바이트 배열로 변환
			dsa.update(strByte); // 서명할 데이터 업데이트
			byte[] realSig = dsa.sign(); // 서명 생성
			output = realSig; // 서명된 결과를 바이트 배열로 저장
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return output; // 서명된 바이트 배열 반환
	}

	// 주어진 공개 키와 데이터를 사용하여 ECDSA 서명을 검증
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC"); // ECDSA 알고리즘 사용
			ecdsaVerify.initVerify(publicKey); // 공개 키로 서명 검증 초기화
			ecdsaVerify.update(data.getBytes()); // 검증할 데이터 업데이트
			return ecdsaVerify.verify(signature); // 서명 검증 결과 반환 (true/false)
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// 키 객체(개인 키나 공개 키)를 Base64 문자열로 변환
	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded()); // Base64로 인코딩 후 문자열로 반환
	}
}
