package kz.iitu.csse.group34.controllers;

import kz.iitu.csse.group34.entities.*;
import kz.iitu.csse.group34.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class MainController {

    @Autowired
    private ItemsRepository itemsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private NewsPostRepository newsPostRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @GetMapping(value = "/")
    public String index(ModelMap model){
        List<NewsPost> newsPosts = newsPostRepository.findAll();

        model.addAttribute("posts", newsPosts);
        return "index";
    }

    @GetMapping(path = "/registration")
    public String registrationPage(ModelMap model){

        return "registration";
    }

    @PostMapping(value = "/register")
    public String register(
            @RequestParam(name = "user_name") String name,
            @RequestParam(name = "user_email") String email,
            @RequestParam(name = "user_password") String password){
        if(password.length()>6){
        Set<Roles> roles = new HashSet<>();
        Roles r = rolesRepository.findById(3L).orElse(null);
        roles.add(r);
        Users user = new Users(null,email,password,name,false,roles);

        userRepository.save(user);
        return "login";}
        else{
            return "login";
        }
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @PostMapping(value = "/addPost")
    public String addPost(
            @RequestParam(name = "post_content") String content,
            @RequestParam(name = "post_short_content") String short_content,
            @RequestParam(name = "post_title") String title
    ){
        if(!content.equals("") && !short_content.equals("") && !title.equals("")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Users user = userRepository.findByEmail(auth.getName());
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Date date = new Date();

            NewsPost post = new NewsPost(null, title, short_content, content, user, date);
            newsPostRepository.save(post);


            return "redirect:/";
        }else{return "redirect:/";}

    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/addComment")
    public String addComment(
            @RequestParam(name = "post_id") Long id,
            @RequestParam(name = "comment") String comment
    ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users user = userRepository.findByEmail(auth.getName());
        NewsPost post = newsPostRepository.findById(id).orElse(null);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date = new Date();

        Comments com = new Comments(null,user,post,comment,date);

        commentsRepository.save(com);



        return "redirect:/postDetails/" + id;
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping(value = "/addModer")
    public String addModer(
            @RequestParam(name = "moder_name") String name,
            @RequestParam(name = "moder_email") String email,
            @RequestParam(name = "moder_password") String password
    ){

        Set<Roles> roles = new HashSet<>();
        Roles r = rolesRepository.findById(2L).orElse(null);
        roles.add(r);
        Users user = new Users(null,email,password,name,true,roles);

        userRepository.save(user);

        return "redirect:/moderators";
    }

    @PostMapping(value = "/addUser")
    public String addUser(
            @RequestParam(name = "user_name") String name,
            @RequestParam(name = "user_email") String email,
            @RequestParam(name = "user_password") String password
    ){

        Set<Roles> roles = new HashSet<>();
        Roles r = rolesRepository.findById(3L).orElse(null);
        roles.add(r);
        Users user = new Users(null,email,password,name,true,roles);

        userRepository.save(user);

        return "redirect:/users";
    }

    @GetMapping(path = "/postDetails/{id}")
    public String detailsPost(ModelMap model, @PathVariable(name = "id") Long id){
        NewsPost post = newsPostRepository.findById(id).orElse(null);
        List<Comments> comments = commentsRepository.findAll();
        List<Comments> postcomments = new ArrayList<>();

        for(Comments c: comments){
            if(c.getNewspost().getId().equals(id)){
                postcomments.add(c);
            }
        }

        model.addAttribute("post",post);
        model.addAttribute("postcomments",postcomments);
        return "details";
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping(path = "/editModer/{id}")
    public String moderEdit(ModelMap model, @PathVariable(name = "id") Long id){

        Users user = userRepository.findById(id).orElse(null);

        model.addAttribute("user",user);

        return "editModer";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/editComment/{id}")
    public String commentEdit(ModelMap model, @PathVariable(name = "id") Long id){

        Comments comment = commentsRepository.findById(id).orElse(null);

        model.addAttribute("comment",comment);

        return "editComment";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @GetMapping(path = "/editPost/{id}")
    public String postEdit(ModelMap model, @PathVariable(name = "id") Long id){

        NewsPost post = newsPostRepository.findById(id).orElse(null);

        model.addAttribute("post",post);

        return "editPost";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @PostMapping(value = "/updatePost")
    public String updatePost(
            @RequestParam(name = "post_id") Long id,
            @RequestParam(name = "post_title") String title,
            @RequestParam(name = "post_content") String content,
            @RequestParam(name = "post_shortContent") String shortContent    ){

        NewsPost post = newsPostRepository.findById(id).orElse(null);

        post.setContent(content);
        post.setShortContent(shortContent);
        post.setTitle(title);

        newsPostRepository.save(post);

        return "redirect:/";
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/updateComment")
    public String updateComment(
            @RequestParam(name = "comment_id") Long id,
            @RequestParam(name = "comment") String comment    ){

        Comments com = commentsRepository.findById(id).orElse(null);
        com.setComment(comment);

        commentsRepository.save(com);

        return "redirect:/postDetails/" + com.getNewspost().getId();
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping(value = "/updateModer")
    public String updateModer(
            @RequestParam(name = "user_id") Long id,
            @RequestParam(name = "user_isActive") Boolean isActive,
            @RequestParam(name = "user_name") String name,
            @RequestParam(name = "user_email") String email,
            @RequestParam(name = "user_password") String password    ){

        Users user = userRepository.findById(id).orElse(null);
        user.setIsActive(isActive);
        user.setEmail(email);
        user.setFullName(name);
        user.setPassword(password);

        userRepository.save(user);

        return "redirect:/moderators";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @GetMapping(path = "/editUser/{id}")
    public String userEdit(ModelMap model, @PathVariable(name = "id") Long id){

        Users user = userRepository.findById(id).orElse(null);
        model.addAttribute("user",user);

        return "editUser";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @PostMapping(value = "/updateUser")
    public String updateUser(
            @RequestParam(name = "user_id") Long id,
            @RequestParam(name = "user_name") String name,
            @RequestParam(name = "user_email") String email,
            @RequestParam(name = "user_password") String password,
            @RequestParam(name = "user_isActive") Boolean isActive){

        Users user = userRepository.findById(id).orElse(null);

        user.setEmail(email);
        user.setFullName(name);
        user.setPassword(password);
        user.setIsActive(isActive);
        userRepository.save(user);

        return "redirect:/users";
    }


    @PreAuthorize("isAnonymous()")
    @GetMapping(path = "/login")
    public String loginPage(Model model){

        return "login";

    }

    @GetMapping(path = "/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePage(Model model){
        List<NewsPost> allPosts = newsPostRepository.findAll();
        List<NewsPost> posts = new ArrayList<>();
        for(NewsPost n: allPosts){
            if(n.getAuthor().equals(getUserData())){
                posts.add(n);
            }
        }


        model.addAttribute("posts", posts);
        model.addAttribute("user", getUserData());
        return "profile";

    }

    @GetMapping(path = "/users")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public String usersPage(Model model){

        model.addAttribute("user", getUserData());

        List<Users> allUsers = userRepository.findAll();
        List<Users> simpleUsers = new ArrayList<>();

        Roles r = rolesRepository.findById(3L).orElse(null);
        for(Users u:allUsers){
            if(u.getRoles().contains(r))
                simpleUsers.add(u);
        }
        model.addAttribute("userList", simpleUsers);

        return "users";

    }


    @GetMapping(path = "/moderators")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String modersPage(Model model){

        model.addAttribute("user", getUserData());

        List<Users> allUsers = userRepository.findAll();
        List<Users> moderators = new ArrayList<>();

        Roles r = rolesRepository.findById(2L).orElse(null);

        for(Users u:allUsers){
            if(u.getRoles().contains(r))
                moderators.add(u);
        }

        model.addAttribute("modersList", moderators);

        return "moderators";

    }


    public Users getUserData(){
        Users userData = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            User secUser = (User)authentication.getPrincipal();
            userData = userRepository.findByEmail(secUser.getUsername());
        }
        return userData;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @PostMapping("/isBlocked")
    public String delete(@RequestParam("idChecked") List<String> idrates){

        if(idrates != null){
            System.out.println("-");
        }
        return "redirect:/moderators";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @GetMapping(path = "/deletePost/{id}")
    public String deletePost(@PathVariable(name = "id") Long id){
        List<Comments> comments = commentsRepository.findAll();

        for(Comments c: comments){
            if(c.getNewspost().getId().equals(id)){
                commentsRepository.delete(c);
            }
        }


        newsPostRepository.deleteById(id);
        return "redirect:/";


    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/deleteComment/{id}")
    public String deleteComment(@PathVariable(name = "id") Long id){


        commentsRepository.deleteById(id);
        return "redirect:/";


    }



}