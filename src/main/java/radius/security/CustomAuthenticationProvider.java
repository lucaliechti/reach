package radius.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import radius.data.repository.UserRepository;

public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
      String username = auth.getName();
      String enteredPassword = auth.getCredentials().toString();
      UserDetails u = this.getUserDetailsService().loadUserByUsername(username);
      if(u == null) {
          return null;
      }
      List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
      final List<String> roles = userRepository.findAuthoritiesByEmail(username);
      for(String role : roles) {
          grantedAuthorities.add(new SimpleGrantedAuthority(role));
      }
      if(encoder.matches(enteredPassword, u.getPassword())) {
          return new UsernamePasswordAuthenticationToken(u, enteredPassword, grantedAuthorities);
      }
      else {
          return null;
      }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
