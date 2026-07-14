동작은 http://localhost:8080/items/new 부터 시작합니다.
상품 ID는 등록할 때마다 1씩 올라가고 카테고리id, 상품명, 상품수량은 직접 입력해야합니다.
등록 버튼을 누르면 mysql 테이블에 입력한 값이 저장됩니다.
그리고 http://localhost:8080/items로 이동하여 목록을 표시합니다.
여기서 수정을 누르면 http://localhost:8080/items/{id}/edit로 이동하여 수정 할 수 있고.
삭제를 누르면 삭제되어 mysql에 변동사항이 반영됩니다.
