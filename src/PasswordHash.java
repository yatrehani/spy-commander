/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to compute Hash values of password
 * @author Yatin Rehani
 * 10-7-2016
 */

public class PasswordHash {
    
    //main method returns computed Hash value
    public static void main(String args[]){
        System.out.println(computeHash(args[0]));
    }
    
    // Method uses MD5 algorithm to compute Hash and returns in HexBinary format
    public static String computeHash(String text){
        try {
            return new String(javax.xml.bind.DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(text.getBytes("UTF-8"))));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PasswordHash.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PasswordHash.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new String();
    }

}
