package radius.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import radius.EmailForm;
import radius.UserForm;
import radius.data.JDBCStaticResourceRepository;

@Controller
@RequestMapping(value={"/", "/home"})
@ComponentScan("radius.config")
@Slf4j
public class HomeController {

	private JDBCStaticResourceRepository staticRepo;
	
	@Autowired
	private StatusController sc;
	
	@Autowired
	public HomeController(JDBCStaticResourceRepository _staticRepo) { this.staticRepo = _staticRepo; }
	
	@RequestMapping(method=GET)
	public String home(@RequestParam(value = "logout", required = false) String loggedout, @RequestParam(value = "error", required = false) String error, Model model, HttpServletRequest req, HttpServletResponse res, Locale locale) {

		log.info("In the HomeController class");
		
		if(SecurityContextHolder.getContext().getAuthentication() != null &&
				SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
				!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) ) {
			return sc.statusPage(model, locale);
		}
		
		if(loggedout != null) {
			model.addAttribute("loggedout", Boolean.TRUE);
		}
		
		if(error != null) {
			model.addAttribute("loginerror", Boolean.TRUE);
		}

		if(model.containsAttribute("success")) {
			model.addAttribute("success", Boolean.TRUE);
		}
		
		return cleanlyHome(model);
	}
	
	@RequestMapping(method=POST)
	public String login(@RequestParam(value = "error", required = false) String loginerror, Model model) {
		System.out.println("in the HomeController class after HTTP POST");
		if(loginerror != null) {
			model.addAttribute("loginerror", Boolean.TRUE);
		}
		return cleanlyHome(model);
	}

	public String cleanlyHome(Model model) {
		model.addAttribute("registrationForm", new UserForm());
		model.addAttribute("cantons", staticRepo.cantons());
		model.addAttribute("newsletterForm", new EmailForm());
		return "home"; //the OG home
	}
}
