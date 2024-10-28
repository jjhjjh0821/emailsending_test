package blockchain;

import java.security.*;
import java.util.ArrayList;

public class Transaction {
	
	public String transactionId; // 트랜잭션 ID (트랜잭션의 해시 값으로 사용)
	public PublicKey sender; // 송신자의 공개 키 (지갑 주소 역할)
	public PublicKey recipient; // 수신자의 공개 키 (지갑 주소 역할)
	public float value; // 송금할 금액
	public byte[] signature; /// 트랜잭션 서명 (해당 트랜잭션이 변조되지 않았음을 증명)
	
	public ArrayList<Transactioninput> inputs = new ArrayList<Transactioninput>(); // 트랜잭션 입력 목록 (이전 트랜잭션의 출력 사용)
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>(); // 트랜잭션 출력 목록 (현재 트랜잭션이 생성하는 새로운 출력)
	
	private static int sequence = 0; // 트랜잭션의 고유한 시퀀스 번호 (트랜잭션 ID 중복 방지를 위해 사용)
	
	/**
	 * 트랜잭션을 생성하는 생성자
	 * 
	 * @param from 송신자의 공개 키 (보내는 사람)
	 * @param to 수신자의 공개 키 (받는 사람)
	 * @param value 송금할 금액
	 * @param inputs 트랜잭션 입력 목록 (이전 트랜잭션의 출력)
	 */
	
	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<Transactioninput> inputs) {
		this.sender = from;  // 송신자의 공개 키 설정
		this.recipient = to; // 수신자의 공개 키 설정
		this.value = value;  // 송금할 금액 설정
		this.inputs = inputs; // 입력 트랜잭션 설정
	}
	
	/**
	 * 트랜잭션 ID (해시 값)를 계산
	 * 
	 * @return 트랜잭션의 고유 해시 값
	 */
	private String calulateHash() {
		sequence++; // 시퀀스를 증가시켜 트랜잭션 ID 중복 방지
		
		// 송신자, 수신자, 금액, 시퀀스를 결합하여 SHA-256 해시를 생성
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(recipient) +
				Float.toString(value) + sequence
				);
	}
	
	/**
	 * 트랜잭션에 서명
	 * 서명된 데이터는 변조되지 않았음을 보장
	 * 
	 * @param privateKey 송신자의 개인 키 (서명을 생성하는 데 사용)
	 */
	public void generateSignature(PrivateKey privateKey) {
		//서명할 데이터는 송신자, 수신자, 금액의 결합
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value)	;

		signature = StringUtil.applyECDSASig(privateKey,data); // ECDSA 서명 알고리즘을 사용하여 서명
	}
	
	/**
	 * 트랜잭션 서명을 검증
	 * 데이터가 변조되지 않았는지 확인
	 * 
	 * @return 서명이 올바르면 true, 그렇지 않으면 false
	 */
	public boolean verifiySignature() {
		// 서명 검증을 위해 송신자, 수신자, 금액을 다시 결합
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value)	;
		return StringUtil.verifyECDSASig(sender, data, signature); // 서명이 올바른지 확인 (ECDSA 검증)
	}
	
	/**
	 * 트랜잭션을 처리
	 * 트랜잭션이 올바른지 확인하고, 그에 따른 작업을 수행
	 * 
	 * @return 트랜잭션이 성공적으로 처리되면 true, 그렇지 않으면 false
	 */
	public boolean processTransaction() {
			
			// 서명이 유효하지 않으면 트랜잭션이 무효
			if(verifiySignature() == false) {
				System.out.println("#Transaction Signature failed to verify");
				return false;
			}
					
			// 입력 트랜잭션을 수집 (미사용 트랜잭션인지 확인)
			for(Transactioninput i : inputs) {
				i.UTXO = BlockchainServlet.UTXOs.get(i.transactionOutputId);
			}

			// 트랜잭션의 입력 값이 최소 트랜잭션 금액보다 작으면 무효 처리
			if(getInputsValue() < BlockchainServlet.minimumTransaction) {
				System.out.println("#Transaction Inputs to small: " + getInputsValue());
				return false;
			}
			
			// 트랜잭션 출력을 생성
			float leftOver = getInputsValue() - value; // 잔액 계산 (입력 값 - 송금할 금액)
			transactionId = calulateHash(); // 트랜잭션 ID 생성
			outputs.add(new TransactionOutput( this.recipient, value,transactionId)); // 수신자에게 금액 전달
			outputs.add(new TransactionOutput( this.sender, leftOver,transactionId));  // 남은 잔액을 송신자에게 반환
					
			// 생성된 출력 트랜잭션을 UTXO(Unspent Transaction Output 사용되지 않은 트랜잭션 출력) 목록에 추가
			for(TransactionOutput o : outputs) {
				BlockchainServlet.UTXOs.put(o.id , o);
			}
			
			// 입력 트랜잭션을 UTXO 목록에서 제거 (사용된 트랜잭션으로 처리)
			for(Transactioninput i : inputs) {
				if(i.UTXO == null) continue; // 트랜잭션이 없으면 스킵
				BlockchainServlet.UTXOs.remove(i.UTXO.id);
			}
			
			return true; // 트랜잭션 처리 성공시 true 반환
		}
		
	/**
	 * 입력 트랜잭션의 총합을 반환
	 * 
	 * @return 입력 값들의 합계
	 */
	public float getInputsValue() {
		float total = 0;
		for(Transactioninput i : inputs) {
			if(i.UTXO == null) continue; // 트랜잭션이 없으면 스킵
			total += i.UTXO.value; // 입력 값의 총합 계산
		}
		return total;
	}
			
}
