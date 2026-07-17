260710
- 사용자의 키오스크 주문 과정을 데이터베이스 구조에 맞게 설계
주문 프로세스: 사용자 메뉴 선택 ->옵션 선택 (사이즈, 맛, 추가 토핑) -> 장바구니 저장 -> 주문 요청 생성 -> 
			주문 정보 저장 -> 주문 상세 내역 저장 -> 결제 완료
전달받은 주문 정보: 
고객 정보, 지점 정보, 키오스크 정보, 주문 방식(매장/포장), 주문 상품 목록, 상품 옵션 정보, 총 결제 금액
클라이언트(Vue)에서 전달하는 주문 데이터를 하나의 객체로 관리하기 위해 DTO 구조 설계
주문 상세 데이터(List)를 객체 형태로 관리

260717
CORS 수정
@CrossOrigin(origins = {"http://localhost:5173","http://192.168.137.173:5173"}) 추가 파일
- LoginController.java
- BranchAdminController.java
- SseController.java
- AdminBranchController.java
