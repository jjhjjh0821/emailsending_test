package blockchain;

public class VoteData {
    private String voteId;      // 투표 ID
    private String voteTime;     // 투표 시간
    private String voterEmail;   // 투표자 이메일
    private String candidateSymbol; // 후보 기호

    // 생성자: 투표 데이터를 초기화하는 메서드
    public VoteData(String voteId, String voterEmail, String candidateSymbol, String voteTime) {
        this.voteId = voteId;
        this.voteTime = voteTime;
        this.voterEmail = voterEmail;
        this.candidateSymbol = candidateSymbol;
    }
    
    // 객체의 문자열 표현을 반환하는 메서드
    @Override
    public String toString() {
        return "VoteData{" +
                "voteId='" + voteId + '\'' +
                ", voteTime='" + voteTime + '\'' +
                ", voterEmail='" + voterEmail + '\'' +
                ", candidateSymbol='" + candidateSymbol + '\'' +
                '}';
    }
    
    // TEST용: voterEmail 세터 메서드 추가
    public void setVoterEmail(String voterEmail) {
        this.voterEmail = voterEmail;
    }
}

