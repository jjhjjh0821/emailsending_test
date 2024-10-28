package blockchain;

//각 입력은 이전 트랜잭션의 출력(TransactionOutput)을 참조
//트랜잭션 입력에 대한 정보를 저장, UTXO(사용되지 않은 트랜잭션 출력)를 통해 이전 트랜잭션의 자금을 가져옴
public class Transactioninput {
	public String transactionOutputId; // 이전 트랜잭션의 출력 ID를 참조하는 변수
	public TransactionOutput UTXO; // 사용되지 않은 트랜잭션 출력 (Unspent Transaction Output)을 저장하는 변수
	
	// 트랜잭션 입력을 생성하고 이전 트랜잭션의 출력 ID를 저장하는 생성자
	public Transactioninput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
