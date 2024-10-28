package blockchain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;


// 지갑에는 사용자의 공개 키와 개인 키가 포함
// 이 키들은 트랜잭션을 서명하고 검증하는 데 사용
public class Wallet {
	public PrivateKey privateKey; // 지갑 소유자의 개인 키
	public PublicKey publicKey; // 지갑 소유자의 공개 키

	// 새로운 키 쌍을 생성하는 메서드
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA",	"BC"); // ECDSA(타원 곡선 디지털 서명 알고리즘)를 사용한 키 쌍 생성기 설정
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); // 보안 난수 생성기
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1"); // 타원 곡선 알고리즘 파라미터

			 // 키 생성기를 초기화하고 키 쌍을 생성
			keyGen.initialize(ecSpec, random); // 256 bytes provides an acceptable security level
			
			// 공개 키와 개인 키 쌍을 생성
			KeyPair keyPair = keyGen.generateKeyPair();
			
			// 생성된 키 쌍에서 개인 키와 공개 키를 설정
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} catch (Exception e) {
			throw new RuntimeException(e); // 예외 발생 시 런타임 예외로 처리
		}
	}
}

