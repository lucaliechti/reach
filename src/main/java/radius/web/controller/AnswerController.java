package radius.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Locale;
import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import radius.data.form.AnswerForm;
import radius.User;
import radius.web.components.ModelDecorator;
import radius.web.service.AnswerService;
import radius.web.service.ConfigService;
import radius.web.service.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping(value="/answers")
public class AnswerController {

	private final ConfigService configService;
	private final MessageSource validationMessageSource;
	private final UserService userService;
	private final ModelDecorator modelDecorator;
	private final AnswerService answerService;

	private static final String ERROR_ANSWER_REGULAR_QUESTIONS = "error.answerRegularQuestions";
	private static final String ERROR_AT_LEAST_ONE_SET = "error.answerOneSetOfQuestions";

	@RequestMapping(method=GET)
	public String answer(Model model, Locale locale) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.findUserByEmail(email).get();
		model.addAttribute("answerForm", answerService.userHasValidlyAnswered(user) ?
				answerService.newFormFromUser(user) : new AnswerForm());
		model.addAllAttributes(modelDecorator.answerAttributes(locale));
		return "answers";
	}
	
	@RequestMapping(method=POST)
	public String answer(@Valid @ModelAttribute("answerForm") AnswerForm answerForm, BindingResult result, Model model,
			Locale locale)  {
		if(result.hasErrors()) {
			model.addAllAttributes(modelDecorator.answerAttributes(locale));
			return "answers";
		} else if(!answerService.validlyAnsweredForm(answerForm)) {
			provideFeedback(result, locale);
			model.addAllAttributes(modelDecorator.answerAttributes(locale));
			return "answers";
		} else {
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			User user = userService.findUserByEmail(email).get();
			answerService.updateUserFromAnswerForm(user, answerForm);
			userService.updateExistingUser(user);
			model.addAllAttributes(userService.userSpecificAttributes(user));
			return "status";
		}
	}

	private void provideFeedback (BindingResult result, Locale locale) {
		if(!configService.specialActive()) {
			addFieldError(result,"regularanswers", ERROR_ANSWER_REGULAR_QUESTIONS, locale);
		} else {
			addFieldError(result, "regularanswers", ERROR_AT_LEAST_ONE_SET, locale);
			addFieldError(result,"specialanswers", ERROR_AT_LEAST_ONE_SET, locale);
		}
	}

	private void addFieldError(BindingResult result, String field, String message, Locale loc) {
		FieldError error = new FieldError("answerForm", field,
		validationMessageSource.getMessage(message, new String[]{}, loc));
		result.addError(error);
	}
}
