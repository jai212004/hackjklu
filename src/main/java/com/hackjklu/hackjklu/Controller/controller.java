package com.hackjklu.hackjklu.Controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hackjklu.hackjklu.entity.UserCred;
import com.hackjklu.hackjklu.entity.resetPassword;
import com.hackjklu.hackjklu.service.service;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.exception.TikaException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Scanner;

@RestController
@RequestMapping("/user")
public class controller {
    private static final String SERPAPI_KEY = "YOUR_SERPAPI_KEY"; // 🔹 Replace with your actual API key

    private final service serv;

    @Autowired
    public controller(service serv) {
        this.serv = serv;
    }

    @GetMapping("/hello")
    public String returnhello(){
        return "hello";
    }


    @PostMapping("/addUser")
    public Boolean addUser(@RequestParam String username, @RequestParam String email, @RequestParam String DOB, @RequestParam String phone, @RequestParam String password) {
        UserCred user = new UserCred(username, email, DOB, password , phone);
        return serv.addUser(user);
    }

    @PostMapping("/validate")
    public void verifyUser(@RequestParam String username , @RequestParam String password , HttpServletResponse response){
        Boolean validation = serv.authenticate(username , password);
        if (validation){
            serv.generateJwt(username , password);
            serv.addToCookie(username , password , response);
            try {
                response.sendRedirect("/uploadresume.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Invalidate session
        }

        // Remove authentication-related cookies
        Cookie cookie = new Cookie("AUTH_COOKIE", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Enable this for HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete immediately
        response.addCookie(cookie);
        try {
            response.sendRedirect("/login.html"); // Redirect to login page
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/resumeret")
    public void getResume(@RequestParam("file") MultipartFile multipart, HttpServletResponse response) throws TikaException, IOException {
        String promptMessage = "By referring to this resume, generate a search query for SerpAPI Google Jobs. Provide only the query value, nothing else. Include the region and years of experience. single profession only with multiple it gets confused" ;

        if (!multipart.getContentType().equalsIgnoreCase("application/pdf")) {
            System.out.println("Invalid file type: " + multipart.getContentType());
            return;
        }

        // 🔹 Extract text from resume
        String resumeText = serv.tikainUse(multipart);

        // 🔹 Generate job search query from Gemini API
        String jobQuery = serv.getPrompted(promptMessage + " " + resumeText);

        // 🔹 Fetch job results
        System.out.println(jobQuery);
        getJobResults(jobQuery , response);
    }

    public void getJobResults(String keyword, HttpServletResponse response) {
        String serapi = "33a8c25563de9e1af2d7b40806080517894c201b851731bc7748c16bd511d082";

        try {
            // ✅ Encode keyword to handle spaces & special characters
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            // ✅ Construct API URL
            String apiUrl = "https://serpapi.com/search.json?engine=google_jobs&q="
                    + encodedKeyword + "&api_key=" + serapi;

            // ✅ Make API Request
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder responseContent = new StringBuilder();
            while (scanner.hasNext()) {
                responseContent.append(scanner.nextLine());
            }
            scanner.close();

            // ✅ Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(responseContent.toString()).getAsJsonObject();
            JsonArray jobs = jsonResponse.getAsJsonArray("jobs_results");

            // ✅ Set response type as HTML
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();

            // ✅ Generate dynamic HTML with CSS for horizontal layout
            out.println("<html><head><title>Job Listings</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; padding: 20px; }");
            out.println(".job-container { display: flex; flex-wrap: nowrap; overflow-x: auto; gap: 15px; padding: 10px; }");
            out.println(".job-card { flex: 0 0 300px; border: 1px solid #ccc; padding: 15px; border-radius: 8px; box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1); background-color: #f9f9f9; }");
            out.println(".job-card h3 a { text-decoration: none; color: #007BFF; }");
            out.println(".job-card h3 a:hover { text-decoration: underline; }");
            out.println(".navbar {\n" +
                    "            background-color: #231834;\n" +
                    "            padding: 15px;\n" +
                    "            text-align: center;\n" +
                    "        }\n" +
                    "\n" +
                    "        .navbar a {\n" +
                    "            color: white;\n" +
                    "            margin: 0 15px;\n" +
                    "            text-decoration: none;\n" +
                    "            font-size: 16px;\n" +
                    "        }\n" +
                    "\n" +
                    "        .navbar a:hover {\n" +
                    "            color: #FF6600;\n" +
                    "        }");
            out.println("</style>");
            out.println("</head><body>");
            out.println("<h1>Job Listings</h1>");
            out.println("<div class='job-container'>");

            // ✅ Loop through job results and display them in cards
            for (int i = 0; i < jobs.size(); i++) {
                JsonObject job = jobs.get(i).getAsJsonObject();
                String title = job.get("title").getAsString();
                String company = job.get("company_name").getAsString();
                String location = job.has("location") ? job.get("location").getAsString() : "Not specified";
                String link = job.has("job_id") ? "https://www.google.com/search?q=" + URLEncoder.encode(title + " " + company, StandardCharsets.UTF_8) : "#";

                out.println("<div class='job-card'>");
                out.println("<h3><a href='" + link + "' target='_blank'>" + title + "</a></h3>");
                out.println("<p><strong>Company:</strong> " + company + "</p>");
                out.println("<p><strong>Location:</strong> " + location + "</p>");
                out.println("</div>");
            }

            out.println("</div></body></html>");
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/checkMail/{email}")
    public Boolean checkEmail(@PathVariable String email){
       return serv.checkEmail(email);
    }

    @PostMapping("/sendMail")
    public void sendEmail(@RequestBody resetPassword resetPasswosrd ) throws MessagingException {
        serv.sendHtmlEmail(resetPasswosrd.getEmailid() , resetPasswosrd.getOtp());
    }

    @PostMapping("/checkOtp")
    public void checkOtp(@RequestParam String email , Integer otp , HttpServletResponse response) throws IOException {
        if (serv.otpValidate(email , otp)){
            response.sendRedirect("/updatePass.html");
        }else {
            response.sendRedirect("/verifyOtp.html");
        }
    }

    @GetMapping("getUserByEmail/{email}")
    public ResponseEntity<?> getUser(@PathVariable String email){
       return serv.findUserByEmail(email);
    }

    @PostMapping("/updatePass")
    public void updatePass(@RequestParam String email , @RequestParam String newpass , HttpServletResponse response) throws IOException {
        if(serv.updatePassword(email , newpass)){
            response.sendRedirect("/login.html");
        }
    }

    @PostMapping("/checkMail")
    public void verifyMail(@RequestParam String email, HttpServletResponse response) throws IOException, MessagingException {
        if (checkEmail(email)) {
            // Generate OTP and send email
            Integer otp = serv.generateRandomFiveDigitNumber();
            serv.sendHtmlEmail(email, otp);

            // Store the OTP in a session or database for later verification
            // session.setAttribute("otp", otp);  // Example for storing in session

            // Redirect to the next step (probably OTP input form)
            response.sendRedirect("/verifyOtp.html");  // Update URL as necessary
        } else {
            // Return an error message if the email is invalid
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);  // Set appropriate error status
            response.getWriter().write("Email not valid");
        }
    }



//    @PostMapping("updatePass")
//    public void checkPassword(@RequestParam String Otp , @RequestParam String)
}