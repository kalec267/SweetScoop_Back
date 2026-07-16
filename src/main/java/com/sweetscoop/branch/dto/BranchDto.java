package com.sweetscoop.branch.dto;

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
}
