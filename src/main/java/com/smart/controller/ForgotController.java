package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	Random random = new Random(1000);
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PasswordEncoder bcrypt;
	
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email")String email, HttpSession session)
	{
		int otp = random.nextInt(10000);
		System.out.println(email + " "+ otp);
		String subject = "OTP from ContactManager";
		String message = "<div style='border:1px solid #e2e2e2; padding:20px;'>"
						+"<h1> OTP is " + "<b>" +otp + "</n>" + "</h1>" + "</div>";
		String to = email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if(flag == true)
		{
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}
		else {
			session.setAttribute("message", "check your mail id!");
		return "forgot_email_form";
		}
	}
	
	@PostMapping("/verify-otp")
	public String verifyotp(@RequestParam("otp") int otp,HttpSession session) {
		int myOtp =(int)session.getAttribute("myotp");
		System.out.println("myotp" + myOtp);
		System.out.println(otp);
		String email = (String)session.getAttribute("email");
		if(otp == myOtp)
		{
			User user = this.userRepository.getUserByUserName(email);
			if(user == null)
			{
				session.setAttribute("message", "User with this mail id doesn't exist");
				return "forgot_email_form";
			}
			return "change_password_form";
		}
		else
		{
			session.setAttribute("message","otp entered is incorrect/invalid");
			return "verify_otp";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newPassword,HttpSession session)
	{
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newPassword));
		this.userRepository.save(user);
		return "redirect:/signin?change=password changed successfully";
	}
}
