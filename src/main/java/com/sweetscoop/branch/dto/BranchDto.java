package com.sweetscoop.branch.dto;

import com.sweetscoop.admin.entity.BranchManager;
import com.sweetscoop.kiosk.dto.KioskDto;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class BranchDto {
    private Integer id;
    private String branchName;
    private String location;
    private List<KioskDto> kiosks;
    private String status;
    
    private String managerId;      // DB 식별 ID (예: BM_001)
    private String managerLoginId; // 로그인용 아이디
    private String managerName;    // 점주 이름
    
    private List<BranchManager> managers;
	public void setManagers(List<BranchManager> managers) {
		this.managers = managers;
	}
	public List<BranchManager> getManagers() {
	    return managers;
	}
}
