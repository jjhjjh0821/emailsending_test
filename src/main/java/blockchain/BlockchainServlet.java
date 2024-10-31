package blockchain;

import user.JWTUtils;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.nio.file.Files;
//import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

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
	
	/**
	 * 메인 : 블록체인의 시작점
	 * 
	 * @param arg
	 */
	/*@Override
    public void init() throws ServletException{
		
		// Bouncy Castle을 보안 프로바이더로 설정 (암호화 알고리즘 제공 라이브러리)
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
		
		// 개선 사항 : 현재 잔액이 없어도 거래가 되고있다 & 트랜잭션과 블록체인 생성이 별개로 이루어지고있다!!!!!!!!!!!!!!!!!
		
		// 두 개의 새로운 지갑을 생성 (둘중 하나는 시스템 지갑 나머지 하나는 사용자 지갑이어야!!!!!!!)
		walletA = new Wallet();
		walletB = new Wallet();

		// 지갑의 공개키와 개인키를 생성 
		walletA.generateKeyPair();
		walletB.generateKeyPair();
		
		// test : 생성된 개인키와 공개키를 출력
		System.out.println("Private and public keys:");
		System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
		System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		
		//테스트를 위한 Transaction생성 (WalletA -> walletB : 100 전송)  
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
        
		//초기 블럭인 '제네시스 블록'을 생성
		blockchain.add(new Block("Genesis block", new VoteData("GenesisVote", "genesis@example.com", "GenesisSymbol", "0"), "0"));
		System.out.println("\nTrying to Mine Genesis block!");
		
		// 제네시스 블록을 마이닝 (난이도에 따라 해시를 찾음)
		blockchain.get(0).mineBlock(difficulty);
		
		// 10개의 블록을 추가로 생성
		for(int i = 1 ; i <= 10 ; i++){
			blockchain.add(new Block("block " + i, blockchain.get(blockchain.size()-1).hash));
			System.out.printf("\nTrying to Mine block #%d", i+1 );
			blockchain.get(i).mineBlock(difficulty);
		}
		
		// 전체 블록체인이 유효한지 체크
		System.out.println("\nBlockchain is Valid : " + isChainValid());
		
		// 블록체인 데이터를 JSON 형식으로 출력
		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		System.out.println("\nOpenchain Block list : ");
		System.out.println(blockchainJson);
		
	}*/
	
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
        
		// voteId가 null이 아니면 블록체인 데이터를 파일에서 로드
	    if (voteId != null) {
	        loadBlockchainFromFile(voteId);
	    } else {
	        System.err.println("voteId가 제공되지 않았습니다.");
	    }
        
        // 블록체인이 존재하는지 확인
        ArrayList<Block> blockchain = blockchainMap.getOrDefault(voteId, new ArrayList<>());

        if(blockchain.isEmpty()) { // 없으면
        	
        	// 제네시스 블록을 생성하여 기본 블록체인에 추가
            VoteData genesisVoteData = new VoteData(voteId, "genesis@example.com", "GenesisSymbol", "0");
            Block genesisBlock = new Block("Genesis block", genesisVoteData, "0");
            genesisBlock.mineBlock(difficulty);
            
            blockchain.add(genesisBlock); // 제네시스 블록을 블록체인에 추가
            blockchainMap.put(voteId, blockchain); // 블록체인 저장
            
            System.out.println("\nTrying to Mine Genesis block!");
        }
        
        // 있으면
        if (symbol != null && voteId != null && voteTime != null && email != null) {
            
            // 첫 번째 유효성 검사: 새블록 생성 전 기존 체인 유효성 검사
            boolean isValid1 = isChainValid(blockchain);
            System.out.println("기존 블록체인 유효성: " + isValid1);
            
            if (!isValid1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"기존 블록체인이 유효하지 않습니다.\", \"isValid1\": false, \"isValid2\": false }");
                out.flush();
                return; // 함수 종료
            }
            
            // 유효하면 블록 생성
            
        	// 투표 정보를 VoteData 객체로 생성
            VoteData voteData = new VoteData(voteId, email, symbol, voteTime);
            
            //System.out.println("투표: " + voteData);
            
            // 새로운 블록 생성
            int blockNumber = blockchain.size(); // 새로운 블록 번호
            String previousHash = blockchain.isEmpty() ? "0" : blockchain.get(blockchain.size() - 1).hash; // 이전 해시
            Block newBlock = new Block("block " + blockNumber, voteData, previousHash);
            newBlock.mineBlock(difficulty); // 블록 마이닝
            blockchain.add(newBlock); // 블록체인에 추가
            blockchainMap.put(voteId, blockchain); // 블록체인 저장
            
            // 해당 투표 ID의 블록체인 데이터를 JSON 형식으로 출력
            String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
            System.out.println("\nOpenchain Block list for Vote ID \"" + voteId + "\": ");
            System.out.println(blockchainJson);
            
            // 두 번째 유효성 검사: 새블록 추가 후 전체 체인의 유효성 검사
            boolean isValid2 = isChainValid(blockchain);
            System.out.println("새 블록체인 유효성: " + isValid2);
            
            if (isValid2) {
            	
                // 블록체인 파일 저장
                saveBlockchainToFile(voteId, blockchain);
                
                response.setStatus(HttpServletResponse.SC_OK);
            	//response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 유효성 테스트 용
                out.write("{\"message\": \"모든 유효성 검사 통과\", \"isValid1\": true, \"isValid2\": true}");
            } else {
                // 유효하지 않은 경우, 에러 메시지 출력
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"새로운 블록체인이 유효하지 않습니다.\", \"isValid1\": true, \"isValid2\": false}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"기호, 보트ID, 투표시간, 이메일이 제공되지 않았습니다.\"}");
        }
        
        out.flush();
    }

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
                System.out.println("유효하지 않은 블록 발견: 블록 #" + i);
                return false;
            }
            
            // 이전 블록의 해시값이 현재 블록의 이전 해시값과 일치하는지 확인
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Previous Hashes not equal");
                System.out.println("유효하지 않은 블록 발견: 블록 #" + i);
                return false;
            }
        }
        return true; // 모든 블록이 유효하면 true를 반환
    }	
    
    // 유효성 검사 & 투표 결과창 반환용
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String voteId = request.getParameter("voteId");

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (voteId != null) {
        	
    		// 블록체인 데이터를 파일에서 로드
        	loadBlockchainFromFile(voteId);
        	
            // 해당 투표 ID에 대한 블록체인 가져오기
            ArrayList<Block> blockchain = blockchainMap.get(voteId);
            
            if (blockchain != null) {
                // 블록체인의 유효성 검사
                boolean isValid = isChainValid(blockchain);
                
                // 유효한 경우 테스트용
                //boolean isValid = true;
                
                // 유효하지 않은 경우 테스트용
                //boolean isValid = false;
                
                if (isValid) {                	
                    // 유효한 경우 블록체인 데이터를 JSON으로 변환
                    String responseJson = new GsonBuilder().setPrettyPrinting().create().toJson(
                            new ValidationResponse(voteId, isValid)
                        );
                        out.write(responseJson); //반환
                	
                	
                } else {
                    // 유효하지 않은 경우 경고 메시지를 포함하여 JSON 응답 작성
                    out.write("{\"voteId\": \"" + voteId + "\", \"isValid\": " + isValid + ", \"error\": \"경고: 블록체인이 유효하지 않습니다.\"}");
                }
                

            } else {
            	boolean isValid = false; // 투표가 진행되지 않아 블록체인이 아예 없는 경우
            	response.setStatus(HttpServletResponse.SC_OK); // Not Found가 아닌 OK로 반환
                out.write("{\"error\": \"경고: 해당 투표에 대한 블록체인이 아직 없습니다.\", \"isValid\": " + isValid + "}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"경고: 투표 ID가 제공되지 않았습니다.\"}");
        }

        out.flush();
    }

    // 블록체인 유효성 검사 결과 반환용 클래스
    private static class ValidationResponse {
        private String voteId;
        private boolean isValid;

        public ValidationResponse(String voteId, boolean isValid) {
            this.voteId = voteId;
            this.isValid = isValid;
        }
    }
    
    // 블록체인 데이터를 JSON 형식으로 파일에 저장하는 메서드
    private void saveBlockchainToFile(String voteId, ArrayList<Block> blockchain) {
    	try {
            String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
            // 지정한 경로에 파일 저장
            String filePath = "C:\\Users\\JunHyuk\\eclipse-workspace\\" + voteId + "_blockchain.json";
            Files.write(Paths.get(filePath), blockchainJson.getBytes());
            System.out.println("블록체인 파일이 저장되었습니다: " + filePath);
        } catch (IOException e) {
            System.err.println("블록체인 파일 저장 중 오류 발생: " + e.getMessage());
        }
    }
    
    
    private void loadBlockchainFromFile(String voteId) {
        try {
            // voteId에 따라 파일 경로 설정
            String filePath = voteId + "_blockchain.json"; 
            String json = new String(Files.readAllBytes(Paths.get(filePath))); // 파일 내용을 읽어옴
            
            // JSON을 블록체인 객체로 변환
            ArrayList<Block> blockchain = new Gson().fromJson(json, new TypeToken<ArrayList<Block>>(){}.getType());
            
            // 블록체인 데이터를 맵에 저장
            blockchainMap.put(voteId, blockchain);
            
            System.out.println("블록체인 데이터가 파일에서 성공적으로 로드되었습니다: " + filePath);
        } catch (IOException e) { // 파일 없는 경우에도 이 오류 생김 -> 실행에는 문제없음
            System.err.println("블록체인 파일 로드 중 오류 발생: " + e.getMessage());
        }
    }
	
}

