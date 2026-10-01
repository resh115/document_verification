package com.documentverification.util;

import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class CsrfUtil {

  private static final SecureRandom RANDOM = new SecureRandom();

  private CsrfUtil() {}

  public static String token(HttpSession session) {
    Object existing = session.getAttribute("csrfToken");
    if (existing != null) return existing.toString();
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    String token = Base64.getUrlEncoder()
      .withoutPadding()
      .encodeToString(bytes);
    session.setAttribute("csrfToken", token);
    return token;
  }

  public static boolean valid(HttpServletRequest request) {
    HttpSession s = request.getSession(false);
    if (s == null) return false;
    Object expected = s.getAttribute("csrfToken");
    String actual = request.getParameter("csrfToken");
    return (
      expected != null && actual != null && expected.toString().equals(actual)
    );
  }
}
