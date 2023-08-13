package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private PasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommondata(Model model, Principal principal)
	{
		String userName = principal.getName(); //login username
		//get user by username
		User user = userRepository.getUserByUserName(userName);
		System.out.println("user = "+ user);
		model.addAttribute("user",user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal)
	{
		model.addAttribute("title","user home");
		return "normal/user_dashboard";
	}
	
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","add contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file ,
			Principal principal, HttpSession session)
	{
		try {
		User user = userRepository.getUserByUserName(principal.getName());
		if(file.isEmpty())
		{
			System.out.println("Empty file received");
			contact.setImage("contact.png");
		}
		else
		{
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		}
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		session.setAttribute("message",new Message("success","contact has been added!..add more."));
		System.out.println("Added to database");
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			session.setAttribute("message",new Message("error", "Something went wrong, try again!"));
		}
		return "normal/add_contact_form";
	}
	
	@GetMapping("/show-contacts/{page}")
	public String viewContact(@PathVariable("page") Integer page,Model model, Principal principal)
	{
		model.addAttribute("title","show contact page");
		User user = userRepository.getUserByUserName(principal.getName());
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	@GetMapping("/contact/{cId}")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model, Principal principal)
	{
		
	    Optional<Contact> contactOptional=this.contactRepository.findById(cId);
	    Contact contact = contactOptional.get();
	    String userName = principal.getName();
	    User user = this.userRepository.getUserByUserName(userName);
	    System.out.println(user.getId());
	    System.out.println(contact.getUser().getId());
	    if(user.getId()==contact.getUser().getId()) {
	    	model.addAttribute("contact",contact);
	    	model.addAttribute("title",contact.getName());
	    }
		return "normal/contact_detail";
	}
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, Principal principal, HttpSession session)
	{
	    Optional<Contact> contactOptional=this.contactRepository.findById(cId);
	    Contact contact = contactOptional.get();
	    String userName = principal.getName();
	    User user = this.userRepository.getUserByUserName(userName);
	    user.getContacts().remove(contact);
	    System.out.println(user.getId());
	    System.out.println(contact.getUser().getId());
//	    if(user.getId()==contact.getUser().getId()) {
//	    	contact.setUser(null);
	    	this.contactRepository.delete(contact);
	    	session.setAttribute("message", new Message("success", "Contact deleted successfully.."));
	    //}
		return "redirect:/user/show-contacts/0";
	}
	
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid ,Model m)
	{
		m.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal principal)
	{
		try {
			Contact oldcontactDetail = this.contactRepository.findById(contact.getCid()).get();
			if(!file.isEmpty())
			{
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file2 = new File(deleteFile,oldcontactDetail.getImage());
				file2.delete();
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldcontactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
		} catch (Exception e) {
			e.printStackTrace();
		}
		session.setAttribute("message",new Message("success", "Your contact has been updated"));
		return "redirect:/user/contact/"+contact.getCid();
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("profile","profile page");
		return "normal/profile";
	}
	
	@GetMapping("/settings")
	public String settings(Model model)
	{
		return "normal/settings";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, 
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session)
	{
		System.out.println("old password " + oldPassword);
		System.out.println("new password " + newPassword);
		
		User currentUser = this.userRepository.getUserByUserName(principal.getName());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message",new Message("success", "Your password has been succesfully updated"));
		}
		else
		{
			session.setAttribute("message",new Message("danger", "Please enter correct old password"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
}
