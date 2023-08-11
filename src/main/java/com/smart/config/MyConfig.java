package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.smart.helper.SessionUtilityBean;

@Configuration
@EnableWebSecurity
public class MyConfig{
	
	@Bean
	public SessionUtilityBean sessionUtilityBean()
	{
		return new SessionUtilityBean();
	}
	
	@Bean
	public UserDetailsService getUserDetailsService()
	{
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public PasswordEncoder encoder() {
	    return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
	    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
	    authenticationProvider.setUserDetailsService(this.getUserDetailsService());
	    authenticationProvider.setPasswordEncoder(encoder());
	    return authenticationProvider;
	}
	
    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration configuration) throws Exception {       
        return configuration.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	http.authorizeHttpRequests().requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/user/**")
    	.hasRole("USER").requestMatchers("/**").permitAll().and().formLogin().loginPage("/signin")
    	.loginProcessingUrl("/doLogin")
    	.defaultSuccessUrl("/user/index")
    	.and().csrf().disable();
       http.authenticationProvider(authenticationProvider());
      return http.build();
    }
}
