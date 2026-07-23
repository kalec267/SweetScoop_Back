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

<<<<<<< HEAD
260717
- 영수증 실시간 출력을 위한 printer 패키지 생성
=======
260719

(회원가입 로직 생성)
[RegisterController.java, RegisterService.java]

(DTO 클래스 분리)
[LoginRequestDto, RegisterSaveDto, LoginResponse]

=======
260722

<<<<<<< HEAD
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


=======

(메뉴관리페이지의 -가격수정 로직 생성)
[MenuAdminController.java, MenuDetailResponse.java, Menu.java‎]
>>>>>>> origin/dev1_hj_back

260723
- 주문취소를 위한 OrderAdminController.java 구현 (admin패키지)
	/**
	     * 주문 취소 및 DB 삭제 API
	     * DELETE /api/admin/orders/{id}
	     */
	    @DeleteMapping("/{id}")
	    public ResponseEntity<Void> deleteOrder(@PathVariable("id") int id) {
	        try {
	            orderService.deleteOrder(id); // Service -> DAO -> MyBatis로 ORDERS 테이블 삭제 수행
	            return ResponseEntity.ok().build();
	        } catch (Exception e) {
	            return ResponseEntity.internalServerError().build();
	        }
	    }
    
	삭제를 위해 OrderDAO.java, OrderDAOImpl.java, OrderService.java, OrderServiceImpl.java, OrderMapper.xml 수정
	외래키를 먼저 삭제 후 데이터 삭제
   	
- 00시마다 웨이팅번호, 주문내역 초기화 스케쥴러 구현 OrderResetScheduler.java
	PaymentMapper.xml에서 selectMaxWaitingNo 부분을 날짜제한을 둬서 날짜가 넘어갈시 웨이팅번호를 1로 설정
