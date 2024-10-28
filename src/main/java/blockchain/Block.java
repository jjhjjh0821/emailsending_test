package blockchain;

import java.util.Date;

public class Block {

	public String hash; // 블록의 해시값
	public String previousHash; // 이전 블록의 해시값
	private String data; // 블록에 저장된 데이터 (트랜잭션 정보 등)
	private VoteData vtdata; // 블록에 저장된 데이터 (투표 정보 등)
	private long timestamp; // 블록이 생성된 시간
	private int nonce; // nonce 값 (마이닝 과정에서 해시 값을 찾기 위한 임시 변수)
	
	/**
	 * 새로운 블럭을 생성하는 생성자
	 * 
	 * @param data data 블록에 포함될 데이터 (트랜잭션 정보 등)
	 * @param previousHash 이전 블록의 해시값
	 */
	public Block(String data, VoteData vtdata, String previousHash) {
		this.data = data;
		this.vtdata = vtdata;
		this.previousHash = previousHash;
		this.timestamp = new Date().getTime();
		this.hash = calculateHash();	//생성시 먼저 hash 값을 하나 만들어 넣어둠
	}
	
	/**
	 * 현재 블록의 해시값을 계산
	 * 해시는 이전 블록의 해시값, 타임스탬프, nonce, 블록 데이터를 사용해 계산
	 * 
	 * @return 계산된 해시값 (SHA-256 적용)
	 */
	public String calculateHash() {
		
		// SHA-256 해시 알고리즘을 사용하여 블록의 해시를 계산
		String calculatedhash = StringUtil.applySha256( 
				previousHash +
				Long.toString(timestamp) +
				Integer.toString(nonce) + // nonce (마이닝 시 증가)
				data +
				vtdata.toString() 
				);
		return calculatedhash; // 계산된 해시값 리턴
	}
	
	/**
	 * 블록을 마이닝하는 메서드
	 * 주어진 난이도에 맞는 해시값을 찾을 때까지 nonce 값을 증가시키며 해시를 계산
	 * 난이도에 따라 목표 해시값 (target)을 설정. ex) difficulty가 3이면 "000"이 target
	 * 
	 * @param difficulty 난이도 (앞에 '0'의 개수로 결정)
	 */
	public void mineBlock(int difficulty) {
		
		// target을 difficulty 숫자 만큼 앞에 0으로 채움.
		String target = new String(new char[difficulty]).replace('\0', '0');
		
		// 현재 해시값의 앞부분이 target과 일치할 때까지 nonce 값을 증가시키며 해시를 다시 계산
		// 생성된 hash가 target과 동일하면 채굴 성공 -> 채굴된 모든 hash가 000으로 시작
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++; // nonce 값을 증가
			hash = calculateHash();  // 새로운 해시를 계산
			
			//System.out.printf("\nGEN Hash #%d : %s", nonce, hash); // 채굴 중간 과정을 출력
		}
		System.out.println("\n채굴 성공!!! : " + hash); // 마이닝에 성공하면 해당 해시값을 출력
	}
}