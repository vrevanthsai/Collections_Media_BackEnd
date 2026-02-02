package com.manga.collectionBend.controllers;

import com.manga.collectionBend.auth.entities.ForgotPassword;
import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.ForgotPasswordRepo;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.auth.utils.ChangePassword;
import com.manga.collectionBend.dto.MailBody;
import com.manga.collectionBend.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/forgotPassword")
public class ForgotPasswordController {

    private final UserRepo userRepo;
    private final EmailService emailService;
    private final ForgotPasswordRepo forgotPasswordRepo;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(UserRepo userRepo, EmailService emailService, ForgotPasswordRepo forgotPasswordRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.emailService = emailService;
        this.forgotPasswordRepo = forgotPasswordRepo;
        this.passwordEncoder = passwordEncoder;
    }

//   1) Send OTP Mail Api
    //    send mail for email verification(which takes email input from client)
    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyEmail(@PathVariable String email){
//        find/valide user provided email exists or not
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide valid email which you registered! "+ email)); // create Custom Exception response

//        extract otp from our custom method
        int otp = otpGenerator();

//        creating the Mailbody object
        MailBody mailBody = MailBody.builder()
                .to(email)
                .subject("OTP for Forgot password Request")
                .text("This is the OTP for your Forgot password Request: " + otp)
                .build();

//    creating object for saving the generated OTP value in ForgotPassword Table
        ForgotPassword fp = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis() + 70 * 1000)) // = 1 min.70 sec for testing( for RealTime= 5 to 10 mins)
                .user(user)
                .build();

//        send mail to user
        emailService.sendSimpleMessage(mailBody);
//        saving otp data
        forgotPasswordRepo.save(fp);

//        sending Response when user clicks submit-button after entering email-input to get OTP in his mail-box
        return ResponseEntity.ok("Email(OTP) send for verification");
    }

//    this method generates a random-6 digit OTP number to send to user
    private Integer otpGenerator(){
        Random random = new Random();
        return random.nextInt(100_000, 999_999); // format for 6-digit OTP with min and max range
    }

//    2) Verify OTP API
//   instead of client sending otp/email from Api-params - use a RequestBody logic like other Post-Apis like createCollection API
//    we need both params to verify OTP associated with User's email
    @PostMapping("/verifyOtp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email){
        //        find/valide user provided email exists or not
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide valid email which you registered!"));

//  fetching already stored data with respect to provided otp and email from user
//     if we get fp-Data then the provided otp value from client is correct
//     or we have empty data in fp-var then otp provided is wrong which is not present in our stored data-records
        ForgotPassword fp = forgotPasswordRepo.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new RuntimeException("Invalid OTP for email: "+ email)); // create Custom Exception response

//        compares stored expiration time range with present time when user clicks for verify OTP-button for varifying his OTP
//       returns True if present time is outside(exceeded) the expiration time range(1min.70sec-from when it was created) or else False
        if(fp.getExpirationTime().before(Date.from(Instant.now()))){
//       as Otp is expired-we delete its record(row) data of that generated OTP from ForgotPassword table-
//       because we can only have one otp data-record for one user - so we delete the expired otp-data-record from table
            forgotPasswordRepo.deleteById(fp.getFpId());
//        we send these res to our UI-frontend where it will ask for re-send otp
            return new ResponseEntity<>("OTP has expired", HttpStatus.EXPECTATION_FAILED); // status code - 417
        }

//        if it passes all above conditions/statements - then provided otp is valid and is within expirationTime range
        return ResponseEntity.ok("OTP verified!");
    }

//    3rd) API for Storing new password
    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePasswordHandler(@RequestBody ChangePassword changePassword, // takes new pwd input from res-body from client
                                                        @PathVariable String email){  // takes email data from url-param
//        checking whether both pwd fields are matching or not
//        or you can validate the same in frontend before sending it
        if(!Objects.equals(changePassword.password(), changePassword.repeatPassword())){ // password() and repeatPassword() are vars of record which stores incoming client data
            return new ResponseEntity<>("Please enter the password again- both are not matching!", HttpStatus.EXPECTATION_FAILED);
        }

//        encrypting the new password
        String encodedPassword = passwordEncoder.encode(changePassword.password());

//        updating the new password in DB-Users table
        userRepo.updatePassword(email, encodedPassword);

        return ResponseEntity.ok("Password changed successfully!");
    }
}
