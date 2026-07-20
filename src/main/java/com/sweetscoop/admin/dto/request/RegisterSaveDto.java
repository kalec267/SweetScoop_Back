package com.sweetscoop.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterSaveDto {
    
    	
    	private String id;
        private String loginId;
        private String password;
        private String name;
        private Integer branchId;

        
        public String getLoginId() {
            return loginId;
        }

        public String getPassword() {
            return password;
        }

        public String getName() {
            return name;
        }

        public Integer getBranchId() {
            return branchId;
        }
}

