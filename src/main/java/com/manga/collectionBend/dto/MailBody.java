package com.manga.collectionBend.dto;

import lombok.Builder;

// In Java, a record is a special type of class designed to act as a transparent carrier for immutable data.
// this record has structure of the mail body content
// to - to which user email id this otp will be sent
// subject - subject of the mail
// text - is the body area of the mail

// record is same like class, but it has its variables declared in its method-args
@Builder
public record MailBody(String to, String subject, String text) {

}
