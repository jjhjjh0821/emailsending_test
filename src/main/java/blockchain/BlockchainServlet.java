package blockchain;

import user.JWTUtils;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/BlockchainServlet")
public class BlockchainServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	// 블록체인 저장을 위한 ArrayList
	private HashMap<String, ArrayList<Block>> blockchainMap = new HashMap<>(); 
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>(); // Unspent Transaction Outputs (UTXOs) 저장을 위한 HashMap
	
	public static int difficulty = 3; // 마이닝 난이도. 숫자가 클수록 마이닝이 어려워짐. 3이상은 좀 오래걸림
	public static float minimumTransaction = 0.1f; // 최소 트랜잭션 금액
	
	// 두 개의 지갑을 생성 (walletA, walletB)
	public static Wallet walletA;
	public static Wallet walletB;
	
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
    	// 클라이언트에서 전달받은 정보 (어떤 투표에 / 누구에게 / 언제 투표했나)
    	String symbol = request.getParameter("symbol");
        String voteId = request.getParameter("voteId");
        String voteTime = request.getParameter("voteTime");
        
        // 누가 투표했나
        String token = request.getHeader("Authorization").replace("Bearer ", ""); // JWT 토큰
        String email = JWTUtils.getEmailFromToken(token); // 토큰에서 이메일 추출
        
        response.setContentType("application/json; charset=UTF-8"); // JSON 형식 및 UTF-8 인코딩 설정
        PrintWriter out = response.getWriter();

        if (symbol != null && voteId != null && voteTime != null && email != null) {
        	
        	// 해당 투표 ID에 대한 블록체인을 가져오거나 새로 생성
            ArrayList<Block> blockchain = blockchainMap.getOrDefault(voteId, new ArrayList<>());
        	
        	// 투표 정보를 VoteData 객체로 생성
            VoteData voteData = new VoteData(voteId, email, symbol, voteTime);
            
            // 새로운 블록 생성
            int blockNumber = blockchain.size()+1; // 새로운 블록 번호
            String previousHash = blockchain.isEmpty() ? "0" : blockchain.get(blockchain.size() - 1).hash; // 이전 해시

            Block newBlock = new Block("block " + blockNumber, voteData, previousHash);
            newBlock.mineBlock(difficulty); // 블록 마이닝
            blockchain.add(newBlock); // 블록체인에 추가
            blockchainMap.put(voteId, blockchain); // 블록체인 저장
            
            // 해당 투표 ID의 블록체인 데이터를 JSON 형식으로 출력
            String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
            System.out.println("\nOpenchain Block list for Vote ID \"" + voteId + "\": ");
            System.out.println(blockchainJson);
            
            // 블록체인 유효성 체크
            boolean isValid = isChainValid(blockchain);
            System.out.println("블록체인 유효성: " + isValid);
            
            if (isValid) {
                response.setStatus(HttpServletResponse.SC_OK);
            	//response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 유효성 테스트 용
                out.write("{\"message\": \"투표가 완료되었습니다: " + symbol + "\", \"email\": \"" + email + "\"}");
            } else {
                // 유효하지 않은 경우, 에러 메시지 출력
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"블록체인이 유효하지 않습니다.\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"기호, 보트ID, 투표시간, 이메일이 제공되지 않았습니다.\"}");
        }
        
        out.flush();
    }
	
	/**
	 * 메인 : 블록체인의 시작점
	 * 
	 * @param arg
	 */
	@Override
    public void init() throws ServletException{

		// Bouncy Castle을 보안 프로바이더로 설정 (암호화 알고리즘 제공 라이브러리)
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
		
		// 두 개의 새로운 지갑을 생성 -> 개선 사항 : 블록체인 생성과 지갑-트랜잭션 동작은 별개로 동작중!!!!!!
		walletA = new Wallet();
		walletB = new Wallet();

		// 지갑의 공개키와 개인키를 생성 -> 개선 사항 : 현재 잔액이 없어도 거래가 되고있다!!!!!!!!!!!!!!!!!
		walletA.generateKeyPair();
		walletB.generateKeyPair();
		
		// test : 생성된 개인키와 공개키를 출력
		System.out.println("Private and public keys:");
		System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
		System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		
		// 테스트를 위한 Transaction생성 (WalletA -> walletB : 100 전송)  
		Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 100, null);
		
		// 트랜잭션에 WalletA의 개인키로 서명
		transaction.generateSignature(walletA.privateKey);
		
		// 서명한 트랜잭션이 올바른지 검증
		System.out.println("Is this Transaction Verify? " + transaction.verifiySignature());
		
		// 제네시스 블록을 생성하여 기본 블록체인에 추가
        String voteId = "genesisVote"; // 기본 투표 ID
        VoteData genesisVoteData = new VoteData(voteId, "genesis@example.com", "GenesisSymbol", "0");
        Block genesisBlock = new Block("Genesis block", genesisVoteData, "0");
        genesisBlock.mineBlock(difficulty);
        
        ArrayList<Block> genesisBlockchain = new ArrayList<>();
        genesisBlockchain.add(genesisBlock);
        blockchainMap.put(voteId, genesisBlockchain); // 제네시스 블록을 저장
        
        System.out.println("\nTrying to Mine Genesis block!");
        
	}
	
	/**
	 * blockchain이 유효한지 체크
	 *  - 현재 블럭의 hash가 유효한 값인지 체크
	 *  - 이전 블록의 해시값과 현재 블록의 연결을 확인
	 *  
	 * @return (유효하면 true, 그렇지 않으면 false)
	 */	
	 // 특정 투표 ID에 대한 블록체인 유효성 체크
    public static Boolean isChainValid(ArrayList<Block> blockchain) {
        Block currentBlock; 
        Block previousBlock;

        for(int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            
            // 현재 블록의 해시값이 올바른지 확인
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");            
                return false;
            }
            
            // 이전 블록의 해시값이 현재 블록의 이전 해시값과 일치하는지 확인
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
        }
        return true; // 모든 블록이 유효하면 true를 반환
    }	
	
}

