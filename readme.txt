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

260717
- 영수증 실시간 출력을 위한 printer 패키지 생성

260721
- firebase 연결을 위한 firebase 패키지 생성
ㄴ resources 파일에 firebase 폴더 생성 후 json 파일 생성

- 영수증 상세메뉴 출력을 위한 PaymentMapper.xml의 selectOrderItems 내용 수정

- 웨이팅 번호 순차 출력을 위한 PaymentMapper.xml의 selectMaxWaitingNo 추가 및 PaymentMapper.java에 Integer selectMaxWaitingNo(); 추가
ㄴ PaymentService.java(순차 번호 적용을 위한 코드 수정)
/*
         * 5. 주문 상태 및 대기번호 갱신 (순차 번호 적용)
         */
        // 데이터베이스에서 현재 가장 큰 웨이팅 번호를 조회해옵니다. (없으면 0 반환)
        Integer maxWaitingNo = paymentMapper.selectMaxWaitingNo();
        
        // 기존 번호가 없으면 1부터 시작, 있으면 +1 증가시킵니다.
        int waitingNo = (maxWaitingNo == null || maxWaitingNo < 0) ? 1 : maxWaitingNo + 1;

        int orderResult =
                paymentMapper.updateOrderStatus(
                        dto.getOrderId(),
                        "결제완료",
                        waitingNo
                );

        if (orderResult <= 0) {
            throw new Exception(
                    "ORDERS 상태 업데이트 실패"
            );
        }
        
        ** git push할 경우 toss key로 인해 보안이 걸려서 사이트에서 보안을 허용해야함(test 선택)
        ㄴ Message Details에 뜨는 문구중 맨 아래로 내려 있는 사이트로 이동 후 허용


