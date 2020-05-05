package cc.mrbird.sso.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author MrBird
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
                .and()
                .logout().logoutUrl("/logout").permitAll()
                .logoutSuccessHandler(
                        (request, response, authentication) -> {
                            String callback = request.getParameter("callback");
                            if (callback == null){
                                callback = "/login?logout";
                            }
                            response.sendRedirect(callback);
                        }
                ).and()
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }
}
