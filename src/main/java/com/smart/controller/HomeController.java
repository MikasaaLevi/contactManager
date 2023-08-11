package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@GetMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title","This is home - Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("title","This is about - Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model)
	{
		model.addAttribute("title","This is register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	@PostMapping("/do_register")
	public String register(@Valid @ModelAttribute("user") User user,BindingResult result1,@RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
			Model model, HttpSession session)
	{
		try {
			if(!agreement)
			{
				System.out.println("You have not agreed to the terms and conditions");
				throw new Exception("Agreement is not accepted");
			}
			if(result1.hasErrors())
			{
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println(agreement);
			System.out.println("User= "+ user);
			userRepository.save(user);
			model.addAttribute("user", new User());
			session.setAttribute("message",new Message("success","Successfully Registered!!"));
			return "signup";
		} catch (Exception e) {
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("danger", "Something went wrong :" +e.getMessage()));
			e.printStackTrace();
			return "signup";
		}
		
	}
	@GetMapping("/signin")
	public String login(Model model)
	{
		model.addAttribute("title","Login -Smart Contact Manager");
		return "login";
	}
}
