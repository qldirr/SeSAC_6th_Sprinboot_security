package codingon.spring_boot_security.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("jwt")
@Setter
@Getter
@Component
public class JwtProperties {
    private String issuer;   // jwt.issuer=admin@spring.security.com
    private String secretkey;  // jwt.secret_key=sesac-springboot-security-admin-key
}

// application.properties 에 있는 설정 값을 가져오는 클래스
