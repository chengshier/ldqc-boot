package org.springblade.modules.auth.dto;
import lombok.Data;
import java.io.Serializable;

@Data
public class AuthUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String password;
    private String checkPassword;
    private String phone;
    private String email;
    private String code;
    private String userId;
    private Integer type;


    private String nickName;
    private String avatar;
}
