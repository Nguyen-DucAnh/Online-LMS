package com.online.lms.constant;

public class ViewNamesConstant {

    public static final String INDEX = "index";
    public static final String LOGIN = "login";
    public static final String REGISTER = "register";
    public static final String VERIFY_OTP = "verify-otp";
    public static final String FORGOT_PASSWORD = "forgot-password";
    public static final String RESET_PASSWORD = "reset-password";
    public static final String GENERIC_ERROR = "error/generic";
    public static final String ERROR_403 = "error/403";
    public static final String ERROR_404 = "error/404";
    public static final String ERROR_500 = "error/500";


    public static final String WALLET_TRANSACTION = "wallet-transactions";
    public static final String WALLET_DEPOSIT = "wallet-deposit";

    public static final String ADMIN_TRANSACTION = "admin/transactions";

    public static final String CHECKOUT = "checkout";

    // Utility methods for building redirects with parameters
    public static String redirectVerifyOtpWithEmail(String email) {
        return "redirect:/verify-otp?email=" + email;
    }

    public static String redirectResetPasswordWithToken(String token) {
        return "redirect:/reset-password?token=" + token;
    }

    // Prevent instantiation
    private ViewNamesConstant() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
