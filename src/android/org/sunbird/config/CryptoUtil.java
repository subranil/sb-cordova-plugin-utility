package org.sunbird.config;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by swayangjit on 30/3/19.
 */
public class CryptoUtil {

  public static String checksum(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = null;
    md = MessageDigest.getInstance("SHA-1");
    md.update(text.getBytes("iso-8859-1"), 0, text.length());
    byte[] sha1hash = md.digest();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < sha1hash.length; i++) {
      sb.append(Integer.toString((sha1hash[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();
  }
}
