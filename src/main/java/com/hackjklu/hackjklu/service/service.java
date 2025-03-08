package com.hackjklu.hackjklu.service;

import com.hackjklu.hackjklu.entity.UserCred;
import com.hackjklu.hackjklu.entity.resetPassword;
import com.hackjklu.hackjklu.repository.repository;
import com.hackjklu.hackjklu.repository.repository2;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.Cookie;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class service {
    public repository repo;
    private final ChatClient client;
    public repository2 repo2;
    @Autowired
    private JavaMailSender mailSender;

    public service(ChatClient.Builder builder){
        this.client = builder.build();
    }

    @Autowired
    public service(repository repo, ChatClient.Builder client , repository2 repo2) {
        this.repo = repo;
        this.repo2 = repo2;
        this.client = client.build();
    }

    public Boolean findByEmail(String email){
        boolean res = false;
        List<UserCred> ls = repo.findAll();
        for (UserCred users : ls){
            if (users.getEmail().equalsIgnoreCase(email)){
                res = true;
            }
        }
        return res;
    }

    public Boolean findByPhone(String phone){
        boolean res = false;
        List<UserCred> ls = repo.findAll();
        for (UserCred users : ls){
            if (users.getPhoneNumber().equalsIgnoreCase(phone)){
                res = true;
            }
        }
        return res;
    }

    public Boolean authenticate(String username , String password){
        Boolean res = false;
        List<UserCred> useridpass = repo.findAll();
        for (UserCred user : useridpass){
            if (user.getUsername().equalsIgnoreCase(username)){
                System.out.println(user.getUsername() + " " + username + " " + user.getPassword() + " " + password);
                if (user.getPassword().equals(password)){
                    res=true;
                }
            }
        }
        return res;
    }

    public ResponseEntity<String> generateJwt(String username , String password){
        String typeoftoken = "jwt";
        String algorithm = "hs256";
        HashMap<String , Object> header = new HashMap<>();
        HashMap<String , Object> claims = new HashMap<>();
        header.put("type" , typeoftoken);
        header.put("algo" , algorithm);
        claims.put("name" , username);
        String secret_key = "jaianmol09818182860579392892829182954044806";
        if (authenticate(username , password)){
            String jwt = Jwts.builder()
                    .setHeader(header)
                    .setClaims(claims)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 60000))
                    .signWith(SignatureAlgorithm.HS256 , secret_key)
                    .compact();
            return ResponseEntity.ok(jwt);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("bad attempt");
    }


    public ResponseEntity<String> addToCookie(String username , String password , HttpServletResponse response){
        ResponseEntity<String> jwt = generateJwt(username , password);
        String jwtstrinig = jwt.getBody();
        if (generateJwt(username , password)!=null){
            Cookie cookie = new Cookie("jwt" , jwtstrinig);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(60);
            cookie.setSecure(true);
            response.addCookie(cookie);
            System.out.println(cookie);
            return ResponseEntity.ok("done");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("not added");
    }

    public String getPrompted(String message){
        return client.prompt(message)
                .call()
                .content();
    }

    public Boolean addUser(UserCred user){
        boolean res = false;
        String email = user.getEmail();
        if (email != null && !findByEmail(email) && !findByPhone(user.phoneNumber)){
            res = true;
            repo.save(user);
        }
        return res;
    }

    public String tikainUse(MultipartFile multipart) throws TikaException, IOException {
        Tika tika = new Tika();
        InputStream inputStream = multipart.getInputStream();
        String out = tika.parseToString(inputStream);
        return out;
    }

    public Boolean checkEmail(String email){
        Boolean bool = false;
        List<UserCred> users = repo.findAll();
        for (UserCred user : users){
            if (user.getEmail().equalsIgnoreCase(email)){
                bool = true;
            }
        }
        return bool;
    }


    public void sendHtmlEmail(String to, Integer subject) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject("hello");
        helper.setText(String.valueOf(subject));// `true` enables HTML
        helper.setFrom("jaianmol84@gmail.com");

        mailSender.send(message);
        resetPassword res = new resetPassword(to , subject);
        repo2.save(res);
    }

    public Boolean otpValidate(String emailId , int otp){
        Boolean bool = false;
        List<resetPassword> ls = repo2.findAll();
        for (resetPassword resetPassword : ls){
            if (resetPassword.getEmailid().equalsIgnoreCase(emailId)){
                if (resetPassword.getOtp() == otp){
                    bool = true;
                }
            }
        }
        return bool;
    }

    public ResponseEntity<?> findUserByEmail(String email) {
        List<UserCred> users = repo.findAll(); // Fetch all users from the database
        for (UserCred user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return ResponseEntity.ok(user); // Return the found user
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"); // If no match is found
    }

    public Boolean updatePassword(String email, String pass) {
        ResponseEntity<UserCred> response = (ResponseEntity<UserCred>) findUserByEmail(email);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            UserCred userCred = response.getBody();
            userCred.setPassword(pass);
            repo.save(userCred); // Persist the updated password in the database
            return true;
        }

        return false;
    }

    public int generateRandomFiveDigitNumber() {
        Random random = new Random();
        return 10000 + random.nextInt(90000); // Generates a number between 10000 and 99999
    }

}
