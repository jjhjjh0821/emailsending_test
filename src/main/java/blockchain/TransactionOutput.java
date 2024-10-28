package blockchain;

import java.security.PublicKey;

// 트랜잭션이 발생한 후 해당 자금이 누구에게 얼마만큼 전송되었는지를 기록
// 이는 UTXO(Unspent Transaction Output)로써 다른 트랜잭션에서 입력으로 사용
public class TransactionOutput {
	public String id; // 트랜잭션 출력의 고유 식별자
	public PublicKey recipient; // 이 트랜잭션 출력의 수신자 (새로운 소유자의 공개 키)
	public float value; //the amount of coins they own
	public String parentTransactionId; // 이 트랜잭션 출력이 생성된 부모 트랜잭션의 ID
	
	// 생성자: 수신자, 값, 부모 트랜잭션 ID를 받아서 트랜잭션 출력을 초기화
	public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
		this.recipient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		
		// 수신자 공개 키, 금액, 부모 트랜잭션 ID를 해시하여 고유 ID 생성
		this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
	}
	
	// 주어진 공개 키가 이 트랜잭션 출력의 소유자인지 확인
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == recipient); // 주어진 공개 키가 수신자와 동일한지 비교
	}
	
}