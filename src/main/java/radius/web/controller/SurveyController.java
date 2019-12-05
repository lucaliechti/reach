package radius.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import radius.User;
import radius.data.form.SurveyForm;
import radius.data.form.UserForm;
import radius.data.repository.*;
import radius.web.components.ModelRepository;
import radius.web.service.UserService;

import javax.validation.Valid;
import java.util.Locale;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@Controller
public class SurveyController {

    private StaticResourceRepository staticRepo;
    private ModelRepository modelRepository;
    private SurveyRepository surveyRepo;
    private NewsletterRepository newsletterRepo;
    private UserService userService;
    private final int SURVEY_SIZE = 15;

    public SurveyController(JSONStaticResourceRepository staticRepo, ModelRepository modelRepository,
                            JDBCSurveyRepository surveyRepo, JDBCNewsletterRepository newsletterRepo,
                            UserService userService) {
        this.staticRepo = staticRepo;
        this.modelRepository = modelRepository;
        this.surveyRepo = surveyRepo;
        this.newsletterRepo = newsletterRepo;
        this.userService = userService;
    }

    @RequestMapping(value="/survey", method=GET)
    public String survey() {
        return "survey";
    }

    @RequestMapping(value="/survey", method=POST)
    public String filledOutSurvey(@ModelAttribute("surveyForm") @Valid SurveyForm surveyForm, BindingResult result,
                                  Model model, Locale loc) {
        if(result.hasErrors()) {
            model.addAttribute("surveyForm", surveyForm);
            return "survey";
        }

        boolean wantsNewsletter = surveyForm.getNewsletter();
        boolean wantsToRegister = surveyForm.getRegistration();

        try {
            surveyRepo.saveAnswers(surveyForm.getQuestions(), surveyForm.getAnswers(), wantsNewsletter, wantsToRegister);
        } catch(Exception e) {
            model.addAttribute("surveyFailure", Boolean.TRUE);
            model.addAttribute("surveyForm", surveyForm);
            return "survey";
        }
        model.addAttribute("surveySuccess", Boolean.TRUE);

        if(wantsToRegister) {
            UserForm userForm = new UserForm();
            userForm.setFirstName(surveyForm.getFirstName());
            userForm.setLastName(surveyForm.getLastName());
            userForm.setCanton(surveyForm.getCanton());
            userForm.setEmail(surveyForm.getEmailR());
            userForm.setPassword(surveyForm.getPassword());

            Optional<User> optionalUser = userService.registerNewUserFromRegistrationForm(userForm);
            if(optionalUser.isEmpty()) {
                model.addAttribute("registrationError", true);
                model.addAllAttributes(modelRepository.homeAttributes());
                return "home";
            }
            boolean success = userService.sendConfirmationEmail(optionalUser.get(), loc);
            if(success) {
                model.addAttribute("waitForEmailConfirmation", Boolean.TRUE);
            } else {
                model.addAttribute("registrationError", true);
            }
            model.addAllAttributes(modelRepository.homeAttributes());
            return "home";
        }

        else if(wantsNewsletter) {
            String emailN = surveyForm.getEmailN();
            try {
                newsletterRepo.subscribe(emailN, "Survey Summer 2019");
            } catch (Exception e) {
                model.addAttribute("surveyFailure", Boolean.TRUE);
                model.addAttribute("surveyForm", surveyForm);
                return "survey";
            }
        }
        model.addAllAttributes(modelRepository.homeAttributes());
        return "home";
    }

    @ModelAttribute
    public void addParams(Model model) {
        model.addAttribute("surveyForm", new SurveyForm());
        model.addAttribute("cantons", staticRepo.cantons());
        model.addAttribute("nrQ", SURVEY_SIZE);
    }
}
